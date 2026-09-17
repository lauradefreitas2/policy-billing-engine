package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.SuspendOverduePoliciesUseCase;
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
import java.util.stream.Stream;

public class SuspendOverduePoliciesUseCaseImpl implements SuspendOverduePoliciesUseCase {

    private static final Logger log = LoggerFactory.getLogger(SuspendOverduePoliciesUseCaseImpl.class);
    private static final int MIN_DAYS_OVERDUE_TO_SUSPEND = 1;

    private final PolicyRepositoryPort policyRepositoryPort;

    public SuspendOverduePoliciesUseCaseImpl(PolicyRepositoryPort policyRepositoryPort) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
    }

    @Override
    public void execute(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate must not be null");

        log.info("Starting overdue policy suspension for date {}", currentDate);

        List<Policy> overdueCandidates = Stream.concat(
                        policyRepositoryPort.findByStatus(PolicyStatus.ACTIVE).stream(),
                        policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT).stream()
                )
                .filter(policy -> daysOverdue(policy, currentDate) >= MIN_DAYS_OVERDUE_TO_SUSPEND)
                .toList();

        log.info("Found {} policies eligible for suspension", overdueCandidates.size());

        LocalDateTime suspendedAt = currentDate.atStartOfDay();
        overdueCandidates.forEach(policy -> suspendPolicy(policy, suspendedAt));

        log.info(
                "Finished overdue policy suspension for date {}. Suspended policies: {}",
                currentDate,
                overdueCandidates.size()
        );
    }

    private long daysOverdue(Policy policy, LocalDate currentDate) {
        LocalDate lastDueDate = resolveLastDueDate(policy.dueDay(), currentDate);
        return ChronoUnit.DAYS.between(lastDueDate, currentDate);
    }

    private LocalDate resolveLastDueDate(int dueDay, LocalDate currentDate) {
        if (currentDate.getDayOfMonth() < dueDay) {
            return currentDate.minusMonths(1).withDayOfMonth(dueDay);
        }
        return currentDate.withDayOfMonth(dueDay);
    }

    private void suspendPolicy(Policy policy, LocalDateTime suspendedAt) {
        policy.suspendDueToNonPayment(suspendedAt);
        policyRepositoryPort.save(policy);
        log.debug("Policy {} suspended due to non-payment", policy.id());
    }
}
