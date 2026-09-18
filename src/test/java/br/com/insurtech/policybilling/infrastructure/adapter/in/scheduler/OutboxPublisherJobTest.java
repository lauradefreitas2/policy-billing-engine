package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.infrastructure.adapter.out.messaging.OutboxEventPublisher;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OutboxPublisherJobTest {

    @Test
    @DisplayName("should invoke outbox publisher")
    void shouldInvokeOutboxPublisher() throws Exception {
        OutboxEventPublisher publisher = mock(OutboxEventPublisher.class);
        OutboxPublisherJob job = new OutboxPublisherJob();
        job.setOutboxEventPublisher(publisher);

        job.executeInternal(mock(JobExecutionContext.class));

        verify(publisher).publishPendingEvents();
    }

    @Test
    @DisplayName("should wrap publisher failure as Quartz job execution exception")
    void shouldWrapPublisherFailure() {
        OutboxEventPublisher publisher = mock(OutboxEventPublisher.class);
        OutboxPublisherJob job = new OutboxPublisherJob();
        job.setOutboxEventPublisher(publisher);
        RuntimeException failure = new RuntimeException("database unavailable");
        doThrow(failure).when(publisher).publishPendingEvents();

        assertThatThrownBy(() -> job.executeInternal(mock(JobExecutionContext.class)))
                .isInstanceOf(JobExecutionException.class)
                .hasCause(failure);
    }
}
