package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InMemoryPolicyRepositoryAdapterTest {

    private final InMemoryPolicyRepositoryAdapter repository = new InMemoryPolicyRepositoryAdapter();

    @Test
    @DisplayName("should save and find policy by id")
    void shouldSaveAndFindPolicyById() {
        Policy policy = buildPolicy();

        Policy saved = repository.save(policy);

        assertThat(saved).isSameAs(policy);
        assertThat(repository.findById(policy.id())).containsSame(policy);
    }

    @Test
    @DisplayName("should return empty when policy is not found")
    void shouldReturnEmptyWhenPolicyIsNotFound() {
        assertThat(repository.findById(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("should throw exception when saving null policy")
    void shouldThrowExceptionWhenSavingNullPolicy() {
        assertThatThrownBy(() -> repository.save(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policy must not be null");
    }

    @Test
    @DisplayName("should throw exception when finding by null id")
    void shouldThrowExceptionWhenFindingByNullId() {
        assertThatThrownBy(() -> repository.findById(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("id must not be null");
    }

    private static Policy buildPolicy() {
        return Policy.issue(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MobileDevice("Brand", "Model", "123456789012345", new BigDecimal("499.90")),
                new BigDecimal("99.90"),
                10
        );
    }
}
