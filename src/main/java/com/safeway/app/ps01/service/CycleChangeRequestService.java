package com.safeway.app.ps01.service;

import static com.safeway.app.ps01.util.CycleScheduleUtility.*;
import static com.safeway.app.ps01.util.DateUtil.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.safeway.app.ps01.domain.CycleChangeRequest;
import com.safeway.app.ps01.domain.CycleSchedule;
import com.safeway.app.ps01.domain.enums.BufferDayEnum;
import com.safeway.app.ps01.domain.enums.ChangeStatusEnum;
import com.safeway.app.ps01.domain.enums.CycleChangeRequestTypeEnum;
import com.safeway.app.ps01.domain.enums.OffsiteIndicatorEnum;
import com.safeway.app.ps01.domain.enums.RunSequenceEnum;
import com.safeway.app.ps01.exception.CycleChangeNotFoundException;
import com.safeway.app.ps01.exception.CycleChangeRequestApprovalException;
import com.safeway.app.ps01.exception.CycleChangeRequestOffsiteException;
import com.safeway.app.ps01.exception.HostPosDatabaseEntryCorruptedException;
import com.safeway.app.ps01.exception.InvalidEffectiveDate;
import com.safeway.app.ps01.exception.MaximumRunSchedulePerRunDateException;
import com.safeway.app.ps01.repository.CycleChangeRequestRepository;
import com.safeway.app.ps01.repository.CycleScheduleRepository;
import com.safeway.app.ps01.util.CycleScheduleUtility;
import com.safeway.app.ps01.util.DateUtil;

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

        Optional<CycleChangeRequest> optional = cycChangeReqRepository.findById(id);

        if (!optional.isPresent()) {
            // TODO: messaging template
            throw new CycleChangeNotFoundException("Cycle Change Request does not exist with ID " + id);
        }

        return optional.get();
    }

    public List<CycleChangeRequest> findAll() {
        return cycChangeReqRepository.findAll();
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

    private boolean validateEffectiveDate(CycleChangeRequest submittedCycleChange) {

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
    public CycleChangeRequest approveCycleChangeRequest(Long id) {

        // TODO: validate if user has the authority to approve

        return cycChangeReqRepository.findById(id).map(existingCycleChange -> {

            if (!existingCycleChange.getChangeStatusName().equals(ChangeStatusEnum.FOR_APPROVAL.getChangeStatus())) {
                throw new CycleChangeRequestApprovalException("Approval Error: Cycle Change should be in for approval status.");
            }

            // TODO: if record exists, copy the record to a new object
            CycleChangeRequest newCycleChange = cloneCycleChangeRequest(existingCycleChange);
            newCycleChange.setId(0l); // remove the id of the current cycle change

            newCycleChange.setChangeStatusName(ChangeStatusEnum.APPROVED.getChangeStatus());
            
            // TODO: this can be removed since default values are in DB2.
            newCycleChange.setExpiryTimestamp(getExpiryTimestamp()); 
            newCycleChange = cycChangeReqRepository.save(newCycleChange);

            existingCycleChange.setExpiryTimestamp(expireNow());
            existingCycleChange.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
            existingCycleChange.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());
            existingCycleChange.setComment("Referenced to new Cycle Change ID: " + newCycleChange.getId());
            cycChangeReqRepository.save(existingCycleChange);

            return newCycleChange;
        }).orElseThrow(() -> new CycleChangeNotFoundException("Can't approve cycle change as the record doesn't exists."));
        // TODO: messaging template
    }

    @Transactional(readOnly = false)
	public CycleChangeRequest rejectCycleChangeRequest(Long id) {

        return cycChangeReqRepository.findById(id).map(forReject -> {

            if (!forReject.getChangeStatusName().equals(ChangeStatusEnum.FOR_APPROVAL.getChangeStatus())) {
                throw new CycleChangeRequestApprovalException("Rejection error: Cycle Change should be in for approval status.");
            }
            
            CycleChangeRequest rejected = cloneCycleChangeRequest(forReject);
            rejected.setId(0l);
            rejected.setChangeStatusName(ChangeStatusEnum.REJECTED.getChangeStatus());

            // TODO: this can be removed since DB2 has default value for this column.
            rejected.setExpiryTimestamp(getExpiryTimestamp());
            rejected = cycChangeReqRepository.save(rejected);

            forReject.setExpiryTimestamp(expireNow());
            forReject.setCycleChangeRequestType(CycleChangeRequestTypeEnum.BASE.getRequestType());
            forReject.setChangeStatusName(ChangeStatusEnum.BASE.getChangeStatus());
            forReject.setComment("Reference to rejected Cycle Change ID: " + rejected.getId());
            cycChangeReqRepository.save(forReject);

            return rejected;
            //TODO: messaging template
        }).orElseThrow(() -> new CycleChangeNotFoundException("Can't reject cycle change as the record doesn't exists."));
	}
}