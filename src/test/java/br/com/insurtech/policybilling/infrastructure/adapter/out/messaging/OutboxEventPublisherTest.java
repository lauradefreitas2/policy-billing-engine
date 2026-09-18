package br.com.insurtech.policybilling.infrastructure.adapter.out.messaging;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.PolicyEventOutboxPersistenceAdapter;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.SpringDataOutboxEventRepository;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import br.com.insurtech.policybilling.infrastructure.config.OutboxProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxEventPublisherTest {

    private static final Instant NOW = Instant.parse("2026-09-18T12:00:00Z");

    @Test
    @DisplayName("should publish ready event and mark it as published")
    void shouldPublishReadyEventAndMarkItAsPublished() throws Exception {
        SpringDataOutboxEventRepository repository = mock(SpringDataOutboxEventRepository.class);
        PolicyEventPublisherPort eventPublisher = mock(PolicyEventPublisherPort.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PolicyCanceledEvent event = event();
        OutboxEventEntity entity = entity(event, objectMapper);
        when(repository.findReadyEvents(any(), any(), any(Pageable.class))).thenReturn(List.of(entity));

        OutboxEventPublisher publisher = new OutboxEventPublisher(
                repository,
                eventPublisher,
                objectMapper,
                meterRegistry,
                properties(5),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        assertThat(publisher.publishPendingEvents()).isEqualTo(1);

        verify(eventPublisher).publishPolicyCanceledEvent(event);
        assertThat(entity.getStatus()).isEqualTo(OutboxEventEntity.PUBLISHED);
        assertThat(entity.getPublishedAt()).isEqualTo(NOW);
        assertThat(meterRegistry.counter("outbox.events", "result", "published").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should retry failures and move event to dead status at the attempt limit")
    void shouldRetryFailuresAndMoveEventToDeadStatusAtAttemptLimit() throws Exception {
        SpringDataOutboxEventRepository repository = mock(SpringDataOutboxEventRepository.class);
        PolicyEventPublisherPort eventPublisher = mock(PolicyEventPublisherPort.class);
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        PolicyCanceledEvent event = event();
        OutboxEventEntity entity = entity(event, objectMapper);
        when(repository.findReadyEvents(any(), any(), any(Pageable.class))).thenReturn(List.of(entity));
        doThrow(new IllegalStateException("broker unavailable"))
                .when(eventPublisher)
                .publishPolicyCanceledEvent(any());
        OutboxEventPublisher publisher = new OutboxEventPublisher(
                repository,
                eventPublisher,
                objectMapper,
                meterRegistry,
                properties(2),
                Clock.fixed(NOW, ZoneOffset.UTC)
        );

        publisher.publishPendingEvents();

        assertThat(entity.getStatus()).isEqualTo(OutboxEventEntity.RETRY);
        assertThat(entity.getAttempts()).isEqualTo(1);
        assertThat(entity.getNextAttemptAt()).isEqualTo(NOW.plusSeconds(10));
        assertThat(entity.getLastError()).isEqualTo("broker unavailable");

        publisher.publishPendingEvents();

        assertThat(entity.getStatus()).isEqualTo(OutboxEventEntity.DEAD);
        assertThat(entity.getAttempts()).isEqualTo(2);
        assertThat(meterRegistry.counter("outbox.events", "result", "retry").count()).isEqualTo(1.0);
        assertThat(meterRegistry.counter("outbox.events", "result", "dead").count()).isEqualTo(1.0);
    }

    private static PolicyCanceledEvent event() {
        return new PolicyCanceledEvent(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), NOW);
    }

    private static OutboxEventEntity entity(PolicyCanceledEvent event, ObjectMapper objectMapper) throws Exception {
        return new OutboxEventEntity(
                event.eventId(),
                event.policyId(),
                PolicyEventOutboxPersistenceAdapter.POLICY_CANCELED_EVENT_TYPE,
                objectMapper.writeValueAsString(event),
                event.canceledAt()
        );
    }

    private static OutboxProperties properties(int maxAttempts) {
        OutboxProperties properties = new OutboxProperties();
        properties.setBatchSize(10);
        properties.setMaxAttempts(maxAttempts);
        properties.setRetryDelay(Duration.ofSeconds(10));
        return properties;
    }
}
