package net.vino9.vinobank;

import net.vino9.vinobank.data.CheckingAccount;
import net.vino9.vinobank.models.FundTransfer;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;
import ulid4j.Ulid;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class Ledger {
    final
    RedisTemplate<String, CheckingAccount> redis;

    final Ulid ulidGenerator = new Ulid();

    public Ledger(RedisTemplate<String, CheckingAccount> template) {
        this.redis = template;
    }

    public Optional<CheckingAccount> getAccountById(String accountId) {
        var fields = redis.opsForHash().entries(CheckingAccount.pk(accountId));
        if (fields.size() == 0) {
            return Optional.empty();
        }

        var account = CheckingAccount.builder()
                .accountId((String) fields.get("account_id"))
                .customerId((String) fields.get("customer_id"))
                .currency((String) fields.get("currency"))
                .status((String) fields.get("status"))
                .balance(BigDecimal.valueOf(Double.parseDouble((String) fields.get("balance"))))
                .availBalance(BigDecimal.valueOf(Double.parseDouble((String) fields.get("avail_balance"))))
                .updatedAt(Timestamp.valueOf(
                        ((String) fields.get("updated_at")).replace("T", " ")
                ))
                .build();
        return Optional.of(account);
    }

    Map<String, String> mapFor(CheckingAccount account) {
        return Map.of(
                "account_id", account.getAccountId(),
                "customer_id", account.getCustomerId(),
                "currency", account.getCurrency(),
                "balance", String.valueOf(account.getBalance()),
                "avail_balance", String.valueOf(account.getAvailBalance()),
                "updated_at", account.getUpdatedAt().toString()
        );
    }


    public FundTransfer localTransfer(CheckingAccount debitAccount, CheckingAccount creditAccount, BigDecimal amount, String memo) {
        var pkDebitAccount = CheckingAccount.pk(debitAccount);
        var pkCreditAccount = CheckingAccount.pk(creditAccount);

        redis.watch(List.of(pkDebitAccount, pkCreditAccount));

        var result = getAccountById(debitAccount.getAccountId());
        var newDebitAccount = result.get();

        result = getAccountById(creditAccount.getAccountId());
        var newCreditAccount = result.get();

        var previousDebitBalance = newDebitAccount.getBalance();
        var newDebitBalance = previousDebitBalance.subtract(amount);
        newDebitAccount.setBalance(newDebitBalance);
        newDebitAccount.setAvailBalance(newDebitBalance);

        var previousCreditBalance = newCreditAccount.getBalance();
        var newCreditBalance = previousCreditBalance.add(amount);
        newCreditAccount.setBalance(newCreditBalance);
        newCreditAccount.setAvailBalance(newCreditBalance);

        List<Object> txResults = redis.execute(new SessionCallback<List<Object>>() {
            public List<Object> execute(RedisOperations ops) throws DataAccessException {
                ops.multi();
                ops.opsForHash().putAll(pkDebitAccount, mapFor(newDebitAccount));
                ops.opsForHash().putAll(pkCreditAccount, mapFor(newCreditAccount));
                return ops.exec();
            }
        });

        return FundTransfer.builder()
                .debitAccountId(newDebitAccount.getAccountId())
                .debitCustomerId(newDebitAccount.getCustomerId())
                .debitPreviousBalance(previousDebitBalance)
                .debitPreviousAvailableBalance(previousDebitBalance)
                .creditCustomerId(newCreditAccount.getCustomerId())
                .creditAccountId(newCreditAccount.getAccountId())
                .creditPreviousBalance(previousCreditBalance)
                .creditPreviousAvailableBalance(previousCreditBalance)
                .debitAccountId(debitAccount.getAccountId())
                .transferAmount(amount)
                .status("success")
                .memo(memo)
                .transactionId(ulidGenerator.next())
                .build();
    }
}
