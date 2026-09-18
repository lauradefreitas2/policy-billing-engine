package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEventEntity {

    public static final String PENDING = "PENDING";
    public static final String RETRY = "RETRY";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String DEAD = "DEAD";

    @Id
    private UUID id;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private int attempts;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "last_error", length = 1000)
    private String lastError;

    @Version
    @Column(nullable = false)
    private long version;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            UUID id,
            UUID aggregateId,
            String eventType,
            String payload,
            Instant occurredAt
    ) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = PENDING;
        this.occurredAt = occurredAt;
        this.createdAt = occurredAt;
        this.nextAttemptAt = occurredAt;
    }

    public void markPublished(Instant publishedAt) {
        this.status = PUBLISHED;
        this.publishedAt = publishedAt;
        this.lastError = null;
    }

    public void markFailed(Instant failedAt, int maxAttempts, Duration retryDelay, String error) {
        attempts++;
        lastError = truncate(error);

        if (attempts >= maxAttempts) {
            status = DEAD;
            return;
        }

        status = RETRY;
        long multiplier = 1L << Math.min(attempts - 1, 20);
        nextAttemptAt = failedAt.plus(retryDelay.multipliedBy(multiplier));
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= 1000) {
            return value;
        }
        return value.substring(0, 1000);
    }

    public UUID getId() {
        return id;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public String getStatus() {
        return status;
    }

    public int getAttempts() {
        return attempts;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public String getLastError() {
        return lastError;
    }
}
