package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class CancelOverduePoliciesUseCaseImpl implements CancelOverduePoliciesUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelOverduePoliciesUseCaseImpl.class);
    private static final int MAX_DAYS_OVERDUE = 10;

    private final PolicyRepositoryPort policyRepositoryPort;
    private final PolicyEventPublisherPort policyEventPublisherPort;

    public CancelOverduePoliciesUseCaseImpl(
            PolicyRepositoryPort policyRepositoryPort,
            PolicyEventPublisherPort policyEventPublisherPort
    ) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
        this.policyEventPublisherPort = Objects.requireNonNull(
                policyEventPublisherPort,
                "policyEventPublisherPort must not be null"
        );
    }

    @Override
    public void execute(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate must not be null");

        log.info("Starting overdue policy cancellation for date {}", currentDate);

        List<Policy> pendingPolicies = policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT);
        log.info("Found {} pending payment policies to evaluate for cancellation", pendingPolicies.size());

        int canceledPolicies = 0;
        for (Policy policy : pendingPolicies) {
            long daysOverdue = calculateDaysOverdue(policy, currentDate);
            if (daysOverdue >= MAX_DAYS_OVERDUE) {
                cancelPolicy(policy, daysOverdue);
                canceledPolicies++;
            }
        }

        log.info(
                "Finished overdue policy cancellation for date {}. Canceled policies: {}",
                currentDate,
                canceledPolicies
        );
    }

    private long calculateDaysOverdue(Policy policy, LocalDate currentDate) {
        LocalDate lastDueDate = resolveLastDueDate(policy.dueDay(), currentDate);
        return ChronoUnit.DAYS.between(lastDueDate, currentDate);
    }

    private LocalDate resolveLastDueDate(int dueDay, LocalDate currentDate) {
        if (currentDate.getDayOfMonth() < dueDay) {
            return currentDate.minusMonths(1).withDayOfMonth(dueDay);
        }
        return currentDate.withDayOfMonth(dueDay);
    }

    private void cancelPolicy(Policy policy, long daysOverdue) {
        policy.cancelDueToNonPayment();
        policyRepositoryPort.save(policy);
        policyEventPublisherPort.publishPolicyCanceledEvent(new PolicyCanceledEvent(
                policy.id(),
                policy.customerId(),
                LocalDateTime.now()
        ));
        log.debug("Policy {} canceled due to {} overdue days", policy.id(), daysOverdue);
    }
}
