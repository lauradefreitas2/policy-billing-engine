package br.com.insurtech.policybilling.infrastructure.adapter.out.messaging;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.PolicyEventOutboxPersistenceAdapter;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.SpringDataOutboxEventRepository;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import br.com.insurtech.policybilling.infrastructure.config.OutboxProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

@Component
public class OutboxEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);
    private static final List<String> READY_STATUSES = List.of(
            OutboxEventEntity.PENDING,
            OutboxEventEntity.RETRY
    );

    private final SpringDataOutboxEventRepository outboxRepository;
    private final PolicyEventPublisherPort eventPublisher;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;
    private final OutboxProperties properties;
    private final Clock clock;

    public OutboxEventPublisher(
            SpringDataOutboxEventRepository outboxRepository,
            PolicyEventPublisherPort eventPublisher,
            ObjectMapper objectMapper,
            MeterRegistry meterRegistry,
            OutboxProperties properties,
            Clock clock
    ) {
        this.outboxRepository = Objects.requireNonNull(outboxRepository, "outboxRepository must not be null");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "eventPublisher must not be null");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper must not be null");
        this.meterRegistry = Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Transactional
    public int publishPendingEvents() {
        Instant now = clock.instant();
        List<OutboxEventEntity> events = outboxRepository.findReadyEvents(
                READY_STATUSES,
                now,
                PageRequest.of(0, properties.getBatchSize())
        );

        events.forEach(event -> publish(event, now));
        return events.size();
    }

    private void publish(OutboxEventEntity event, Instant now) {
        try {
            eventPublisher.publishPolicyCanceledEvent(deserialize(event));
            event.markPublished(now);
            meterRegistry.counter("outbox.events", "result", "published").increment();
            log.info("Published outbox event {} for policy {}", event.getId(), event.getAggregateId());
        } catch (RuntimeException ex) {
            event.markFailed(
                    now,
                    properties.getMaxAttempts(),
                    properties.getRetryDelay(),
                    rootMessage(ex)
            );
            String result = OutboxEventEntity.DEAD.equals(event.getStatus()) ? "dead" : "retry";
            meterRegistry.counter("outbox.events", "result", result).increment();
            log.warn(
                    "Failed to publish outbox event {} on attempt {}. New status: {}",
                    event.getId(),
                    event.getAttempts(),
                    event.getStatus(),
                    ex
            );
        }
    }

    private PolicyCanceledEvent deserialize(OutboxEventEntity event) {
        if (!PolicyEventOutboxPersistenceAdapter.POLICY_CANCELED_EVENT_TYPE.equals(event.getEventType())) {
            throw new IllegalArgumentException("Unsupported outbox event type: " + event.getEventType());
        }

        try {
            return objectMapper.readValue(event.getPayload(), PolicyCanceledEvent.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize outbox event " + event.getId(), ex);
        }
    }

    private static String rootMessage(RuntimeException exception) {
        Throwable cause = exception;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
    }
}
