package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.infrastructure.adapter.out.messaging.OutboxEventPublisher;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@DisallowConcurrentExecution
public class OutboxPublisherJob extends QuartzJobBean {

    private OutboxEventPublisher outboxEventPublisher;

    @Autowired
    public void setOutboxEventPublisher(OutboxEventPublisher outboxEventPublisher) {
        this.outboxEventPublisher = Objects.requireNonNull(
                outboxEventPublisher,
                "outboxEventPublisher must not be null"
        );
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            outboxEventPublisher.publishPendingEvents();
        } catch (RuntimeException ex) {
            throw new JobExecutionException(ex);
        }
    }
}
