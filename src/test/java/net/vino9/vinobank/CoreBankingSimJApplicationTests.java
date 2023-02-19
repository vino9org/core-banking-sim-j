package net.vino9.vinobank;

import net.vino9.vinobank.models.FundTransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

@SpringBootTest
class CoreBankingSimJApplicationTests {

    @Autowired
    Ledger ledger;

    @Autowired
    FundTransferService fundTransferService;

    @Test
    void contextLoads() {
    }

    @Test
    void canPerformTransfer() {
        var debitAccount = "ACC_0000017";
        var creditAccount = "ACC_0000004";
        var amount = BigDecimal.valueOf(100.0);

        var result = ledger.getAccountById(debitAccount);
        assert result.isPresent();

        var previousBalance = result.get().getBalance();

        result = ledger.getAccountById(creditAccount);
        assert result.isPresent();

        var transferRequest = FundTransferRequest.builder()
                .accountId(debitAccount)
                .customerId("ACC_0000017")
                .creditAccountId("ACC_0000004")
                .amount(amount)
                .memo("test transfer")
                .build();

        var response = fundTransferService.invokeFundTransfer(transferRequest);
        assert response.getStatusCode().is2xxSuccessful();

        result = ledger.getAccountById(debitAccount);
        assert result.isPresent();
        assert result.get().getBalance().add(amount).compareTo(previousBalance) == 0;
    }
}
