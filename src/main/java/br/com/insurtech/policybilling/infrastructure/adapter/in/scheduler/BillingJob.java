package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Objects;

@Component
@DisallowConcurrentExecution
public class BillingJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(BillingJob.class);

    private ProcessDailyBillingUseCase processDailyBillingUseCase;

    @Autowired
    public void setProcessDailyBillingUseCase(ProcessDailyBillingUseCase processDailyBillingUseCase) {
        this.processDailyBillingUseCase = Objects.requireNonNull(
                processDailyBillingUseCase,
                "processDailyBillingUseCase must not be null"
        );
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LocalDate currentDate = LocalDate.now();
        log.info("Billing job started for date {}", currentDate);

        try {
            processDailyBillingUseCase.execute(currentDate);
            log.info("Billing job finished successfully for date {}", currentDate);
        } catch (RuntimeException ex) {
            log.error("Billing job failed for date {}", currentDate, ex);
            throw new JobExecutionException(ex);
        }
    }
}
