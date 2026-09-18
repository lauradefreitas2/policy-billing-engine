package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventOutboxPort;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

public class CancelOverduePoliciesUseCaseImpl implements CancelOverduePoliciesUseCase {

    private static final Logger log = LoggerFactory.getLogger(CancelOverduePoliciesUseCaseImpl.class);
    private static final int MAX_DAYS_SUSPENDED = 10;

    private final PolicyRepositoryPort policyRepositoryPort;
    private final PolicyEventOutboxPort policyEventOutboxPort;
    private final Clock clock;

    public CancelOverduePoliciesUseCaseImpl(
            PolicyRepositoryPort policyRepositoryPort,
            PolicyEventOutboxPort policyEventOutboxPort,
            Clock clock
    ) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
        this.policyEventOutboxPort = Objects.requireNonNull(
                policyEventOutboxPort,
                "policyEventOutboxPort must not be null"
        );
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public void execute(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate must not be null");

        log.info("Starting overdue policy cancellation for date {}", currentDate);

        List<Policy> suspendedPolicies = policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED);
        log.info("Found {} suspended policies to evaluate for cancellation", suspendedPolicies.size());

        int canceledPolicies = 0;
        for (Policy policy : suspendedPolicies) {
            long daysSuspended = calculateDaysSuspended(policy, currentDate);
            if (daysSuspended >= MAX_DAYS_SUSPENDED) {
                cancelPolicy(policy, daysSuspended);
                canceledPolicies++;
            }
        }

        log.info(
                "Finished overdue policy cancellation for date {}. Canceled policies: {}",
                currentDate,
                canceledPolicies
        );
    }

    private long calculateDaysSuspended(Policy policy, LocalDate currentDate) {
        return ChronoUnit.DAYS.between(policy.suspendedAt().toLocalDate(), currentDate);
    }

    private void cancelPolicy(Policy policy, long daysSuspended) {
        policy.cancelDueToNonPayment();
        policyRepositoryPort.save(policy);
        policyEventOutboxPort.appendPolicyCanceledEvent(new PolicyCanceledEvent(
                java.util.UUID.randomUUID(),
                policy.id(),
                policy.customerId(),
                clock.instant()
        ));
        log.debug("Policy {} canceled due to {} suspended days", policy.id(), daysSuspended);
    }
}
