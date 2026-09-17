package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.SuspendOverduePoliciesUseCase;
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
public class SuspensionJob extends QuartzJobBean {

    private static final Logger log = LoggerFactory.getLogger(SuspensionJob.class);

    private SuspendOverduePoliciesUseCase suspendOverduePoliciesUseCase;

    @Autowired
    public void setSuspendOverduePoliciesUseCase(SuspendOverduePoliciesUseCase suspendOverduePoliciesUseCase) {
        this.suspendOverduePoliciesUseCase = Objects.requireNonNull(
                suspendOverduePoliciesUseCase,
                "suspendOverduePoliciesUseCase must not be null"
        );
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LocalDate currentDate = LocalDate.now();
        log.info("Suspension job started for date {}", currentDate);

        try {
            suspendOverduePoliciesUseCase.execute(currentDate);
            log.info("Suspension job finished successfully for date {}", currentDate);
        } catch (RuntimeException ex) {
            log.error("Suspension job failed for date {}", currentDate, ex);
            throw new JobExecutionException(ex);
        }
    }
}
