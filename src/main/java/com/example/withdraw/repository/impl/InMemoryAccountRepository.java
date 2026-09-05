package com.example.withdraw.repository.impl;

import com.example.withdraw.entity.Account;
import com.example.withdraw.repository.AccountRepository;
import io.vavr.control.Option;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory {@link AccountRepository} for the demo.
 *
 * <p>Seeds one account ({@code user-1} / balance {@code 100.00} / card
 * {@code 1234567812345678}) so the REST API is exercisable out of the box.</p>
 */
@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    public InMemoryAccountRepository() {
        accounts.put("user-1", new Account("user-1", new BigDecimal("100.00"), "1234567812345678"));
    }

    @Override
    public Option<Account> findById(String userId) {
        return Option.of(accounts.get(userId));
    }

    @Override
    public void save(Account account) {
        accounts.put(account.userId(), account);
    }
}
