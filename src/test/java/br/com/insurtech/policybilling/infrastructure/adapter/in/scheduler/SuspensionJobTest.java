package br.com.insurtech.policybilling.infrastructure.adapter.in.scheduler;

import br.com.insurtech.policybilling.application.port.in.SuspendOverduePoliciesUseCase;
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
class SuspensionJobTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-06-21T12:00:00Z"),
            ZoneOffset.UTC
    );

    @Mock
    private SuspendOverduePoliciesUseCase suspendOverduePoliciesUseCase;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Test
    @DisplayName("should invoke use case when job is executed")
    void shouldInvokeUseCaseWhenJobIsExecuted() throws Exception {
        SuspensionJob suspensionJob = new SuspensionJob();
        suspensionJob.setSuspendOverduePoliciesUseCase(suspendOverduePoliciesUseCase);
        suspensionJob.setClock(FIXED_CLOCK);

        suspensionJob.executeInternal(jobExecutionContext);

        verify(suspendOverduePoliciesUseCase).execute(LocalDate.of(2026, 6, 21));
    }

    @Test
    @DisplayName("should wrap use case failure as Quartz job execution exception")
    void shouldWrapUseCaseFailureAsQuartzJobExecutionException() {
        SuspensionJob suspensionJob = new SuspensionJob();
        suspensionJob.setSuspendOverduePoliciesUseCase(suspendOverduePoliciesUseCase);
        suspensionJob.setClock(FIXED_CLOCK);
        RuntimeException failure = new RuntimeException("suspension failed");
        doThrow(failure).when(suspendOverduePoliciesUseCase).execute(any(LocalDate.class));

        assertThatThrownBy(() -> suspensionJob.executeInternal(jobExecutionContext))
                .isInstanceOf(JobExecutionException.class)
                .hasCause(failure);
    }
}
