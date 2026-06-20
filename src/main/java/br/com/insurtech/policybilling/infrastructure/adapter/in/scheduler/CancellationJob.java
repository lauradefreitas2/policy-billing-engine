package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
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
public class CancellationJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(CancellationJob.class);

    private CancelOverduePoliciesUseCase cancelOverduePoliciesUseCase;

    @Autowired
    public void setCancelOverduePoliciesUseCase(CancelOverduePoliciesUseCase cancelOverduePoliciesUseCase) {
        this.cancelOverduePoliciesUseCase = Objects.requireNonNull(
                cancelOverduePoliciesUseCase,
                "cancelOverduePoliciesUseCase must not be null"
        );
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LocalDate currentDate = LocalDate.now();
        log.info("Cancellation job started for date {}", currentDate);

        try {
            cancelOverduePoliciesUseCase.execute(currentDate);
            log.info("Cancellation job finished successfully for date {}", currentDate);
        } catch (RuntimeException ex) {
            log.error("Cancellation job failed for date {}", currentDate, ex);
            throw new JobExecutionException(ex);
        }
    }
}
