package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingJobTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-21T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private ProcessDailyBillingUseCase processDailyBillingUseCase;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Test
    @DisplayName("should invoke use case when job is executed")
    void shouldInvokeUseCaseWhenJobIsExecuted() throws Exception {
        BillingJob billingJob = new BillingJob();
        billingJob.setProcessDailyBillingUseCase(processDailyBillingUseCase);
        billingJob.setClock(FIXED_CLOCK);

        billingJob.executeInternal(jobExecutionContext);

        verify(processDailyBillingUseCase).execute(LocalDate.of(2026, 6, 21));
    }

    @Test
    @DisplayName("should wrap use case failure as Quartz job execution exception")
    void shouldWrapUseCaseFailureAsQuartzJobExecutionException() {
        BillingJob billingJob = new BillingJob();
        billingJob.setProcessDailyBillingUseCase(processDailyBillingUseCase);
        billingJob.setClock(FIXED_CLOCK);
        RuntimeException failure = new RuntimeException("billing failed");
        doThrow(failure).when(processDailyBillingUseCase).execute(any(LocalDate.class));

        assertThatThrownBy(() -> billingJob.executeInternal(jobExecutionContext))
                .isInstanceOf(JobExecutionException.class)
                .hasCause(failure);
    }
}
