package com.example.withdrawal.domain;

/**
 * Persistence contract for {@link Transaction} entities.
 */
public interface TransactionRepository {

    Transaction save(Transaction transaction);
}
