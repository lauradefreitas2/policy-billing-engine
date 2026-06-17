package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingJobTest {

    @Mock
    private ProcessDailyBillingUseCase processDailyBillingUseCase;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Test
    @DisplayName("should invoke use case when job is executed")
    void shouldInvokeUseCaseWhenJobIsExecuted() throws Exception {
        // Given
        BillingJob billingJob = new BillingJob();
        billingJob.setProcessDailyBillingUseCase(processDailyBillingUseCase);

        // When
        billingJob.executeInternal(jobExecutionContext);

        // Then
        verify(processDailyBillingUseCase).execute(any(LocalDate.class));
    }
}
