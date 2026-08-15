package com.example.withdrawal.repository;

import com.example.withdrawal.domain.Transaction;
import com.example.withdrawal.domain.TransactionRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread-safe in-memory transaction store with {@link AtomicLong} id generation.
 */
@Repository
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<String, Transaction> store = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public Transaction save(Transaction transaction) {
        if (transaction.getId() == null) {
            transaction.setId("txn-" + idGenerator.incrementAndGet());
        }
        store.put(transaction.getId(), transaction);
        return transaction;
    }
}
