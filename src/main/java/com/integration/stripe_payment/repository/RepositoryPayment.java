package com.integration.stripe_payment.repository;

import com.integration.stripe_payment.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface RepositoryPayment extends JpaRepository<Payment,Long>{
    Optional<Payment> findByIdepotency(String idepotency);
    Optional<Payment> findById(UUID id);

}
