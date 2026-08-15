package com.example.withdrawal.domain;

/**
 * Persistence contract for {@link Account} entities.
 */
public interface AccountRepository {

    Account findByUserId(String userId);

    Account save(Account account);
}
