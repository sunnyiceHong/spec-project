package com.example.withdrawal.repository;

import com.example.withdrawal.domain.Account;
import com.example.withdrawal.domain.AccountRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe in-memory account store keyed by {@code userId}.
 */
@Repository
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> store = new ConcurrentHashMap<>();

    @Override
    public Account findByUserId(String userId) {
        return store.get(userId);
    }

    @Override
    public Account save(Account account) {
        store.put(account.getUserId(), account);
        return account;
    }
}
