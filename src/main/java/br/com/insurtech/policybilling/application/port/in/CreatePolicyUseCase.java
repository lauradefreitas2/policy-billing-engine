package br.com.insurtech.policybilling.application.port.in;

import br.com.insurtech.policybilling.domain.model.Policy;

public interface CreatePolicyUseCase {

    Policy execute(Policy policy);
}
