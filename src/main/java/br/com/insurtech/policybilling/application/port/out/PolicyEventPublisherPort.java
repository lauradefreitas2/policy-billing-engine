package br.com.insurtech.policybilling.application.port.out;

public interface PolicyEventPublisherPort {

    void publishPolicyCanceledEvent(PolicyCanceledEvent event);
}
