package br.com.insurtech.policybilling.application.port.in;

import java.time.LocalDate;

public interface CancelOverduePoliciesUseCase {

    void execute(LocalDate currentDate);
}
