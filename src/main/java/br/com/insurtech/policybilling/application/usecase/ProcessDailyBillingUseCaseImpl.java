package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ProcessDailyBillingUseCaseImpl implements ProcessDailyBillingUseCase {

    private static final Logger log = LoggerFactory.getLogger(ProcessDailyBillingUseCaseImpl.class);

    private final PolicyRepositoryPort policyRepositoryPort;

    public ProcessDailyBillingUseCaseImpl(PolicyRepositoryPort policyRepositoryPort) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
    }

    @Override
    public void execute(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate must not be null");

        int dueDay = currentDate.getDayOfMonth();
        log.info("Starting daily billing processing for date {} and due day {}", currentDate, dueDay);

        List<Policy> duePolicies = policyRepositoryPort.findByDueDayAndStatus(dueDay, PolicyStatus.ACTIVE);
        log.info("Found {} active policies due for billing day {}", duePolicies.size(), dueDay);

        int processedPolicies = 0;
        for (Policy policy : duePolicies) {
            if (policy.isDueForBilling(currentDate)) {
                markPolicyAsPendingPayment(policy);
                processedPolicies++;
            }
        }

        log.info("Finished daily billing processing for date {}. Processed policies: {}", currentDate, processedPolicies);
    }

    private void markPolicyAsPendingPayment(Policy policy) {
        policy.markAsPendingPayment();
        policyRepositoryPort.save(policy);
        log.debug("Policy {} marked as pending payment", policy.id());
    }
}
