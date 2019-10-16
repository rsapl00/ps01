package com.albertsons.app.ps01.service;

import static com.albertsons.app.ps01.util.CycleScheduleUtility.*;
import static com.albertsons.app.ps01.util.DateUtil.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.CycleSchedule;
import com.albertsons.app.ps01.domain.enums.BufferDayEnum;
import com.albertsons.app.ps01.domain.enums.ChangeStatusEnum;
import com.albertsons.app.ps01.domain.enums.CycleChangeRequestTypeEnum;
import com.albertsons.app.ps01.domain.enums.OffsiteIndicatorEnum;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.exception.CycleChangeNotFoundException;
import com.albertsons.app.ps01.exception.CycleChangeRequestApprovalException;
import com.albertsons.app.ps01.exception.CycleChangeRequestCancelException;
import com.albertsons.app.ps01.exception.CycleChangeRequestOffsiteException;
import com.albertsons.app.ps01.exception.HostPosDatabaseEntryCorruptedException;
import com.albertsons.app.ps01.exception.InvalidEffectiveDate;
import com.albertsons.app.ps01.exception.MaximumRunSchedulePerRunDateException;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.repository.CycleScheduleRepository;
import com.albertsons.app.ps01.util.CycleScheduleUtility;
import com.albertsons.app.ps01.util.DateUtil;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CycleChangeRequestService {

    final private CycleChangeRequestRepository cycChangeReqRepository;
    final private CycleScheduleRepository cycleScheduleRepository;

    public CycleChangeRequestService(final CycleChangeRequestRepository repository,
            final CycleScheduleRepository cycleScheduleRepository) {
        this.cycChangeReqRepository = repository;
        this.cycleScheduleRepository = cycleScheduleRepository;
    }

    /**
     * 
     * @param divId
     * @param startRunDate
     * @param endRunDate
     * @return
     */
    public List<CycleChangeRequest> findCycleChangeRequestByDivIdAndRunDate(final String divId, final Date startRunDate,
            final Date endRunDate) {
        return cycChangeReqRepository.findByDivIdAndRunDateBetweenOrderByRunDateAsc(divId, startRunDate, endRunDate);
    }

    /**
     * 
     * @param divId
     * @param startRunDate
     * @param endRunDate
     * @return
     */
    @Transactional(readOnly = false)
    public List<CycleChangeRequest> generateCycleChangeRequest(final String divId, final Date startRunDate,
            final Date endRunDate) {

        // retrieve cycle change requests by division and run date range.
        final List<CycleChangeRequest> cycleChangeRequests = findCycleChangeRequestByDivIdAndRunDate(divId,
                startRunDate, endRunDate);

        final List<CycleChangeRequest> newCycleChangeRequests = generateCycleChangeRequestBasedOnCycleSchedule(divId,
                startRunDate, endRunDate, cycleChangeRequests);

        saveCycleChangeRequests(newCycleChangeRequests);

        return findCycleChangeRequestByDivIdAndRunDate(divId, startRunDate, endRunDate);
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> saveCycleChangeRequests(final List<CycleChangeRequest> cycleChangeRequests) {
        return cycChangeReqRepository.saveAll(cycleChangeRequests);
    }

    /**
     * Generate Cycle Change Request base on the given date range. Skip generation
     * if date is already exists in the database.
     * 
     */
    public List<CycleChangeRequest> generateCycleChangeRequestBasedOnCycleSchedule(final String divId,
            final Date startRunDate, final Date endRunDate, final List<CycleChangeRequest> cycleChangeRequests) {

        final List<CycleSchedule> defaultCycleSchedules = cycleScheduleRepository.findByDivIdOrderByDayNumAsc(divId);

        final List<CycleChangeRequest> newSchedules = new ArrayList<>();

        // Set the end date to end date + 1 so that the end date will be included in the
        // generation.
        final LocalDate afterEndRunDate = (endRunDate.toLocalDate()).plusDays(1);

        for (LocalDate date = startRunDate.toLocalDate(); date.isBefore(afterEndRunDate); date = date.plusDays(1)) {

            final LocalDate currentDateInLoop = date;

            // Generate if the current date is not yet in Cycle Change Request
            if (!isRunDateExists(currentDateInLoop, cycleChangeRequests)) {
                defaultCycleSchedules.stream().forEach(cycleSchedule -> {
                    newSchedules
                            .addAll(CycleScheduleUtility.generateCycleChangeRequest(cycleSchedule, currentDateInLoop));
                });
            }
        }

        return newSchedules;
    }

    public CycleChangeRequest findById(final Long id) {
        return cycChangeReqRepository.findById(id).orElseThrow(() -> {
            throw new CycleChangeNotFoundException("Cycle Change Request not found.");
        });
    }

    public List<CycleChangeRequest> findAll() {
        return cycChangeReqRepository.findAll();
    }

    @Transactional(readOnly = false)
    public CycleChangeRequest saveNewCycleChangeRequest(final CycleChangeRequest newCycleChange) {
        // retrieve cycle change request by run date and not expired.
        final List<CycleChangeRequest> cycleChangeRequests = cycChangeReqRepository.findByDivIdAndRunDateAndNotExpired(
                newCycleChange.getDivId(), newCycleChange.getRunDate(), DateUtil.getExpiryTimestamp());

        final List<CycleChangeRequest> newCycleChangeRequests = new ArrayList<>();

        if (cycleChangeRequests.isEmpty()) {
            newCycleChangeRequests.add(
                    cycChangeReqRepository.save(createNewCycleChangeRequest(newCycleChange, RunSequenceEnum.FIRST)));
        } else {
            cycleChangeRequests.forEach(cycle -> {
                newCycleChangeRequests.add(cycChangeReqRepository
                        .save(createNewCycleChangeRequest(newCycleChange, RunSequenceEnum.SECOND)));
            });
        }

        if (newCycleChangeRequests.isEmpty()) {
            // TODO: Messaging template
            throw new HostPosDatabaseEntryCorruptedException(
                    "Possible data corruption in Database entry. Look for a one (1) active run date with run number is set to 2.");
        }

        return newCycleChangeRequests.size() > 0 ? newCycleChangeRequests.get(0) : null;
    }

    @Transactional(readOnly = false)
    public CycleChangeRequest saveCycleChangeRequest(final CycleChangeRequest newCycleChange) {

        // retrieve cycle change request by run date and not expired.
        final List<CycleChangeRequest> cycleChangeRequests = cycChangeReqRepository.findByDivIdAndRunDateAndNotExpired(
                newCycleChange.getDivId(), newCycleChange.getRunDate(), DateUtil.getExpiryTimestamp());

        final List<CycleChangeRequest> newCycleChangeRequests = new ArrayList<>();

        if (cycleChangeRequests.isEmpty()) {
            // 1. Insert if there is no other (Run 1) for the same date.

            // validation of effective date
            if (!validateEffectiveDate(newCycleChange)) {
                throw new InvalidEffectiveDate(
                        "Invalid Effective Date. Review your request and compare it from the current schedule.");
            }

            newCycleChangeRequests.add(
                    cycChangeReqRepository.save(createNewCycleChangeRequest(newCycleChange, RunSequenceEnum.FIRST)));

        } else {
            if (cycleChangeRequests.size() >= RunSequenceEnum.SECOND.getRunSequence()) {
                // TODO: messaging template
                throw new MaximumRunSchedulePerRunDateException(
                        "Maximum schedule per run date reached. Only two (2) same run date is accepted.");
            }

            cycleChangeRequests.forEach(cycle -> {

                if (isEqual(cycle.getRunDate(), newCycleChange.getRunDate())
                        && isAfter(cycle.getEffectiveDate(), newCycleChange.getEffectiveDate())) {
                    // TODO: messaging template
                    throw new InvalidEffectiveDate("Invalid Effective Date. "
                            + "Specified run date has an effective date that is later than the request.");
                }

                if (!validateEffectiveDate(newCycleChange)) {
                    throw new InvalidEffectiveDate(
                            "Invalid effective date. Review your request and compare it from the current schedule.");
                }

                // 2. If There is Run 1 non-offsite.
                // 2.1 Insert Run 2.
                if ((RunSequenceEnum.FIRST.getRunSequence() == cycle.getRunNumber().intValue())
                        && cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {

                    newCycleChangeRequests.add(cycChangeReqRepository
                            .save(createNewCycleChangeRequest(newCycleChange, RunSequenceEnum.SECOND)));

                }

                // 3. if there is run 1 Offsite
                // 3.1 Insert Run 2 if it is Offsite
                // 3.2 Throw error if Run 2 is not offsite.
                if ((RunSequenceEnum.FIRST.getRunSequence() == cycle.getRunNumber().intValue())
                        && cycle.getOffsiteIndicator().equals(OffsiteIndicatorEnum.OFFSITE.getIndicator())) {

                    // if Run 1 is OFFSITE, RUN 2 should also be OFFSITE. else throw Exception
                    if (newCycleChange.getOffsiteIndicator().equals(OffsiteIndicatorEnum.NON_OFFSITE.getIndicator())) {
                        // TODO: Messaging template
                        throw new CycleChangeRequestOffsiteException(
                                "Invalid offsite schedule. You cannot request Offsite on Run1 only for a 2 run cycle. Offsite can be Run2 ONLY or Run1 AND Run2 for a 2 run cycle.");
                    }

                    newCycleChangeRequests.add(cycChangeReqRepository
                            .save(createNewCycleChangeRequest(newCycleChange, RunSequenceEnum.SECOND)));
                }
            });
        }

        if (newCycleChangeRequests.isEmpty()) {
            // TODO: Messaging template
            throw new HostPosDatabaseEntryCorruptedException(
                    "Possible data corruption in Database entry. Look for a one (1) active run date with run number is set to 2.");
        }

        return newCycleChangeRequests.size() > 0 ? newCycleChangeRequests.get(0) : null;
    }

    private boolean validateEffectiveDate(final CycleChangeRequest submittedCycleChange) {

        Date runDate = submittedCycleChange.getRunDate();

        List<CycleChangeRequest> prevWeek = cycChangeReqRepository.findActiveByDivIdAndBetweenRunDatesDesc(
                submittedCycleChange.getDivId(), DateUtil.getBufferDate(runDate, BufferDayEnum.MINUS_BUFFER), runDate,
                DateUtil.getExpiryTimestamp());

        List<CycleChangeRequest> nextWeek = cycChangeReqRepository.findActiveByDivIdAndBetweenRunDatesAsc(
                submittedCycleChange.getDivId(), runDate, DateUtil.getBufferDate(runDate, BufferDayEnum.PLUS_BUFFER),
                DateUtil.getExpiryTimestamp());

        return validateCycleChangeEffectiveDate(prevWeek, nextWeek, submittedCycleChange);
    }

    @Transactional(readOnly = false)
    public CycleChangeRequest approveCycleChangeRequest(final Long id) {

        // TODO: validate if user has the authority to approve

        return cycChangeReqRepository.findById(id).map(existingCycleChange -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.APPROVED, existingCycleChange);
        }).orElseThrow(
                () -> new CycleChangeNotFoundException("Can't approve cycle change as the record doesn't exists."));
        // TODO: messaging template
    }

    @Transactional(readOnly = false)
    public CycleChangeRequest rejectCycleChangeRequest(Long id) {

        return cycChangeReqRepository.findById(id).map(forReject -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.REJECTED, forReject);
            // TODO: messaging template
        }).orElseThrow(
                () -> new CycleChangeNotFoundException("Can't reject cycle change as the record doesn't exists."));
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> approveMultipleCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.APPROVED, cycle);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> rejectMultipleCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.REJECTED, cycle);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> forApprovalCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.FOR_APPROVAL, cycle);
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> cancelCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {

            if (cycle.getChangeStatusName().equals(ChangeStatusEnum.APPROVED.getChangeStatus())
                    || cycle.getChangeStatusName().equals(ChangeStatusEnum.REJECTED.getChangeStatus())
                    || cycle.getChangeStatusName().equals(ChangeStatusEnum.BASE.getChangeStatus())) {
                throw new CycleChangeRequestCancelException(
                        "Cancelation error: Only SAVED/FOR APPROVAL can be canceled.");
            }

            CycleChangeRequest canceled = cloneCycleChangeRequest(cycle);
            canceled.setId(0l);
            canceled.setCycleChangeRequestType(CycleChangeRequestTypeEnum.CANCEL.getRequestType());

            // TODO: this can be removed since default values are in DB2.
            canceled.setExpiryTimestamp(getExpiryTimestamp());
            canceled = cycChangeReqRepository.save(canceled);

            cycle.setExpiryTimestamp(expireNow());
            cycle.setComment("Referenced to new Cycle Change ID: " + canceled.getId());

            return canceled;
        }).collect(Collectors.toList());
    }

    private List<CycleChangeRequest> searchCycleChangesByIds(final List<Long> ids) {
        List<CycleChangeRequest> existingCycleChanges = cycChangeReqRepository.findByIdIn(ids);

        if (existingCycleChanges.size() < ids.size()) {
            throw new CycleChangeNotFoundException("Record not found on one or more cycle change request.");
        }

        return existingCycleChanges;
    }

    private CycleChangeRequest returnApprovedOrRejectCycleChange(final ChangeStatusEnum changeType,
            final CycleChangeRequest existingCycleChange) {

        if (changeType == ChangeStatusEnum.FOR_APPROVAL) {
            if (!existingCycleChange.getChangeStatusName().equals(ChangeStatusEnum.SAVED.getChangeStatus())) {
                throw new CycleChangeRequestApprovalException(
                        "For Approval Error: Cycle Change should be in SAVED status.");
            }
        } else if ((changeType == ChangeStatusEnum.APPROVED || changeType == ChangeStatusEnum.REJECTED)) {
            if (!existingCycleChange.getChangeStatusName().equals(ChangeStatusEnum.FOR_APPROVAL.getChangeStatus())) {

                String message = "Approval";
                if (ChangeStatusEnum.REJECTED == changeType) {
                    message = "Rejection";
                }
                throw new CycleChangeRequestApprovalException(
                        message + " Error: Cycle Change should be in FOR APPROVAL status.");
            }
        }

        CycleChangeRequest newApprovedCycle = cloneCycleChangeRequest(existingCycleChange);
        newApprovedCycle.setId(0l);
        newApprovedCycle.setChangeStatusName(changeType.getChangeStatus());

        // TODO: this can be removed since default values are in DB2.
        newApprovedCycle.setExpiryTimestamp(getExpiryTimestamp());
        newApprovedCycle = cycChangeReqRepository.save(newApprovedCycle);

        existingCycleChange.setExpiryTimestamp(expireNow());
        existingCycleChange.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
        existingCycleChange.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());
        existingCycleChange.setComment("Referenced to new Cycle Change ID: " + newApprovedCycle.getId());

        return newApprovedCycle;
    }

    @Transactional(readOnly = false)
    public CycleChangeRequest updateCycleChangeRequest(final CycleChangeRequest cycleChangeRequest) {

        final List<CycleChangeRequest> existingCycles = cycChangeReqRepository.findByDivIdAndRunDateAndNotExpired(
                cycleChangeRequest.getDivId(), cycleChangeRequest.getRunDate(), DateUtil.getExpiryTimestamp());

        return cycChangeReqRepository.findById(cycleChangeRequest.getId()).map(toBeUpdatedCycle -> {

            CycleChangeRequest newUpdateRequest = cloneCycleChangeRequest(toBeUpdatedCycle);
            newUpdateRequest.setId(0l); // remove id to create a new record
            newUpdateRequest.setRunDate(cycleChangeRequest.getRunDate());
            newUpdateRequest.setEffectiveDate(cycleChangeRequest.getEffectiveDate());
            newUpdateRequest.setOffsiteIndicator(cycleChangeRequest.getOffsiteIndicator());

            if (existingCycles.isEmpty()) {
                newUpdateRequest = cycChangeReqRepository
                        .save(createNewCycleChangeRequest(newUpdateRequest, RunSequenceEnum.FIRST));
            } else {

                // if (isEqual(toBeUpdatedCycle.getRunDate(), newUpdateRequest.getRunDate())) {

                // existingCycles.stream().forEach(cycle -> {
                // if (cycle.getRunNumber().equals(RunSequenceEnum.SECOND.getRunSequence())
                // && !cycle.getId().equals(toBeUpdatedCycle.getId())) {

                // if (isAfter(toBeUpdatedCycle.getEffectiveDate(), cycle.getEffectiveDate())) {
                // throw new InvalidEffectiveDate(
                // "Effective Date of Run 2 is later than the request's effective date.");
                // }

                // }
                // });

                newUpdateRequest = cycChangeReqRepository.save(createNewCycleChangeRequest(newUpdateRequest,
                        RunSequenceEnum.getRunSequenceEnum(toBeUpdatedCycle.getRunNumber())));

                // }
            }

            toBeUpdatedCycle.setExpiryTimestamp(expireNow());
            toBeUpdatedCycle.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
            toBeUpdatedCycle.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());
            toBeUpdatedCycle.setComment("Referenced to new Cycle Change ID: " + newUpdateRequest.getId());

            return newUpdateRequest;

        }).orElseThrow(() ->

        {
            throw new CycleChangeNotFoundException("Cycle Change Request not found.");
        });
    }
}