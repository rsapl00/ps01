package com.albertsons.app.ps01.service;

import static com.albertsons.app.ps01.util.CycleScheduleUtility.cloneCycleChangeRequest;
import static com.albertsons.app.ps01.util.CycleScheduleUtility.createNewCycleChangeRequest;
import static com.albertsons.app.ps01.util.CycleScheduleUtility.isRunDateExists;
import static com.albertsons.app.ps01.util.DateUtil.expireNow;
import static com.albertsons.app.ps01.util.DateUtil.getExpiryTimestamp;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.domain.CycleSchedule;
import com.albertsons.app.ps01.domain.enums.ChangeStatusEnum;
import com.albertsons.app.ps01.domain.enums.CycleChangeRequestTypeEnum;
import com.albertsons.app.ps01.domain.enums.RunSequenceEnum;
import com.albertsons.app.ps01.exception.CycleChangeNotFoundException;
import com.albertsons.app.ps01.exception.CycleChangeRequestApprovalException;
import com.albertsons.app.ps01.exception.CycleChangeRequestCancelException;
import com.albertsons.app.ps01.exception.HostPosDatabaseEntryCorruptedException;
import com.albertsons.app.ps01.repository.CycleChangeRequestRepository;
import com.albertsons.app.ps01.repository.CycleScheduleRepository;
import com.albertsons.app.ps01.security.userdetails.User;
import com.albertsons.app.ps01.util.CycleScheduleUtility;
import com.albertsons.app.ps01.util.DateUtil;

import org.springframework.security.core.context.SecurityContextHolder;
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
        return cycChangeReqRepository.findById(id).orElseThrow(() -> new CycleChangeNotFoundException("Cycle Change not found."));
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
    public CycleChangeRequest approveCycleChangeRequest(final Long id) {

        // TODO: send email notification

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
            // TODO: send email notification
            // TODO: messaging template
        }).orElseThrow(
                () -> new CycleChangeNotFoundException("Can't reject cycle change as the record doesn't exists."));
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> approveMultipleCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.APPROVED, cycle);
            // TODO: send email notification
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> rejectMultipleCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.REJECTED, cycle);
            // TODO: send email notification
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> forApprovalCycleChangeRequest(final List<Long> ids) {
        return searchCycleChangesByIds(ids).stream().map(cycle -> {
            return returnApprovedOrRejectCycleChange(ChangeStatusEnum.FOR_APPROVAL, cycle);
            // TODO: send email notification
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = false)
    public List<CycleChangeRequest> cancelCycleChangeRequest(final List<Long> ids) {

        final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return searchCycleChangesByIds(ids).stream().map(cycle -> {

            if (cycle.getChangeStatusName().equals(ChangeStatusEnum.APPROVED.getChangeStatus())
                    || cycle.getChangeStatusName().equals(ChangeStatusEnum.REJECTED.getChangeStatus())
                    || cycle.getChangeStatusName().equals(ChangeStatusEnum.BASE.getChangeStatus())) {
                throw new CycleChangeRequestCancelException(
                        "Cancelation error: Only SAVED/FOR APPROVAL can be canceled.");
            }

            if (!cycle.getCreateUserId().equals(user.getUsername())) {
                throw new CycleChangeRequestCancelException("You can't cancel other user's request.");
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

                newUpdateRequest = cycChangeReqRepository.save(createNewCycleChangeRequest(newUpdateRequest,
                        RunSequenceEnum.getRunSequenceEnum(toBeUpdatedCycle.getRunNumber())));

            }

            toBeUpdatedCycle.setExpiryTimestamp(expireNow());
            toBeUpdatedCycle.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
            toBeUpdatedCycle.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());
            toBeUpdatedCycle.setComment("Referenced to new Cycle Change ID: " + newUpdateRequest.getId());

            return newUpdateRequest;

        }).orElseThrow(() -> new CycleChangeNotFoundException("Cycle Change not found."));
        
    }
}