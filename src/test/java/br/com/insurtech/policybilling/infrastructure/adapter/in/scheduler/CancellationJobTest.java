package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CancellationJobTest {

    @Mock
    private CancelOverduePoliciesUseCase cancelOverduePoliciesUseCase;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Test
    @DisplayName("should invoke use case when job is executed")
    void shouldInvokeUseCaseWhenJobIsExecuted() throws Exception {
        CancellationJob cancellationJob = new CancellationJob();
        cancellationJob.setCancelOverduePoliciesUseCase(cancelOverduePoliciesUseCase);

        cancellationJob.executeInternal(jobExecutionContext);

        verify(cancelOverduePoliciesUseCase).execute(any(LocalDate.class));
    }

    @Test
    @DisplayName("should wrap use case failure as Quartz job execution exception")
    void shouldWrapUseCaseFailureAsQuartzJobExecutionException() {
        CancellationJob cancellationJob = new CancellationJob();
        cancellationJob.setCancelOverduePoliciesUseCase(cancelOverduePoliciesUseCase);
        RuntimeException failure = new RuntimeException("cancellation failed");
        doThrow(failure).when(cancelOverduePoliciesUseCase).execute(any(LocalDate.class));

        assertThatThrownBy(() -> cancellationJob.executeInternal(jobExecutionContext))
                .isInstanceOf(JobExecutionException.class)
                .hasCause(failure);
    }
}
