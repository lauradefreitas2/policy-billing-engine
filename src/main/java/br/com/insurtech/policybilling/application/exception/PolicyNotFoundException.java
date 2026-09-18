package br.com.insurtech.policybilling.application.exception;

import java.util.UUID;

public class PolicyNotFoundException extends RuntimeException {

    public PolicyNotFoundException(UUID policyId) {
        super("Policy not found: " + policyId);
    }
}
