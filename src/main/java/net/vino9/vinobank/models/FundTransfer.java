package net.vino9.vinobank.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class FundTransfer {
    @JsonProperty("transaction_id")
    String transactionId;

    @JsonProperty("debit_customer_id")
    String debitCustomerId;
    @JsonProperty("debit_account_id")
    String debitAccountId;
    @JsonProperty("debit_prev_balance")
    BigDecimal debitPreviousBalance;
    @JsonProperty("debit_prev_avail_balance")
    BigDecimal debitPreviousAvailableBalance;
    @JsonProperty("debit_balance")
    BigDecimal debitBalance;
    @JsonProperty("debit_avail_balance")
    BigDecimal debitAvailableBalance;

    @JsonProperty("credit_customer_id")
    String creditCustomerId;
    @JsonProperty("credit_account_id")
    String creditAccountId;
    @JsonProperty("credit_prev_balance")
    BigDecimal creditPreviousBalance;
    @JsonProperty("credit_prev_avail_balance")
    BigDecimal creditPreviousAvailableBalance;
    @JsonProperty("credit_balance")
    BigDecimal creditBalance;
    @JsonProperty("credit_avail_balance")
    BigDecimal creditAvailableBalance;

    @JsonProperty("transfer_amount")
    BigDecimal transferAmount;
    String currency;
    String memo;
    @JsonProperty("transaction_date")
    String transactionDate;
    String status;

    @JsonProperty("ref_id")
    String referenceId;
}
