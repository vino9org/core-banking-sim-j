package net.vino9.vinobank;

import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import net.vino9.vinobank.data.CheckingAccount;
import net.vino9.vinobank.models.FundTransferRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "video not found")
class AccountFoundException extends RuntimeException {
}

@RestController
@Slf4j
public class FundTransferService {

    final Ledger ledger;

    public FundTransferService(Ledger ledger) {
        this.ledger = ledger;
    }


    @GetMapping("/core-banking/accounts/{account-id}")
    public CheckingAccount getAccountById(@PathVariable("account-id") String accountId) {
        var result = ledger.getAccountById(accountId);
        if (result.isPresent()) {
            return result.get();
        }
        throw new AccountFoundException();
    }


    @PostMapping("/core-banking/local-transfers")
    public ResponseEntity invokeFundTransfer(@RequestBody FundTransferRequest request) {
        log.info("request: {}", request);

        var amount = request.getAmount();

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new ResponseEntity("invalid amount", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var result = ledger.getAccountById(request.getAccountId());
        if (result.isEmpty()) {
            return new ResponseEntity("invalid debit account", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        var debitAccount = result.get();
        if (debitAccount.getAvailBalance().compareTo(amount) < 0) {
            return new ResponseEntity("insufficient fund in debit account", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        result = ledger.getAccountById(request.getCreditAccountId());
        if (result.isEmpty()) {
            return new ResponseEntity("invalid credit account", HttpStatus.UNPROCESSABLE_ENTITY);
        }
        var creditAccount = result.get();

        var transfer = ledger.localTransfer(debitAccount, creditAccount, amount, request.getMemo());

        return new ResponseEntity(transfer, HttpStatus.OK);
    }
}
