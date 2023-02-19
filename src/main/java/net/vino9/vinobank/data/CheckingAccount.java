package net.vino9.vinobank.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.redis.core.RedisHash;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@RedisHash
@Builder
public class CheckingAccount {
    public static final String PK_PREFIX = ":core_banking.models.CheckingAccount:";

    @JsonProperty("account_id")
    String accountId;
    @JsonProperty("customer_id")
    String customerId;
    String currency;
    String status;
    BigDecimal balance;
    @JsonProperty("avail_balance")
    BigDecimal availBalance;
    @JsonProperty("updated_at")
    Timestamp updatedAt;

    public static String pk(CheckingAccount instance) {
        return PK_PREFIX + instance.getAccountId();
    }

    public static String pk(String id) {
        return PK_PREFIX + id;
    }
}
