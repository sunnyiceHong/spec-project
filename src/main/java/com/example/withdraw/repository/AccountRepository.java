package com.example.withdraw.repository;

import com.example.withdraw.entity.Account;
import io.vavr.control.Option;

/**
 * Port for loading and persisting accounts. The withdrawal service depends on
 * this interface, never on a concrete store.
 */
public interface AccountRepository {

    Option<Account> findById(String userId);

    void save(Account account);
}
