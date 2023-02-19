package net.vino9.vinobank.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundTransferRequest {
    @JsonProperty("debit_customer_id")
    String customerId;
    @JsonProperty("debit_account_id")
    String accountId;
    @JsonProperty("credit_account_id")
    String creditAccountId;
    BigDecimal amount;
    String currency;
    @JsonProperty("customer_id")
    String transactionDate;
    String memo;
    @JsonProperty("ref_id")
    String referenceId;
}
