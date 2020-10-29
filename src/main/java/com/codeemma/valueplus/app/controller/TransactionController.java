package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.model.TransactionModel;
import com.codeemma.valueplus.domain.service.abstracts.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static com.codeemma.valueplus.domain.util.UserUtils.getLoggedInUser;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Validated
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/transactions", produces = MediaType.APPLICATION_JSON_VALUE)
public class TransactionController {
    private final TransferService transferService;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public TransactionModel initiate(@Valid @RequestBody PaymentRequestModel request) throws ValuePlusException {
        return transferService.transfer(getLoggedInUser(), request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getAllTransfers(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return transferService.getAllTransactions(pageable);
    }

    @PreAuthorize("hasAnyRole('AGENT')")
    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getUserTransfers(@PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return transferService.getAllUserTransactions(getLoggedInUser(), pageable);
    }

    @GetMapping("/reference/{reference}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TransactionModel> getUserTransfers(@PathVariable("reference") String reference) throws ValuePlusException {
        return transferService.getTransactionByReference(getLoggedInUser(), reference);
    }

    @GetMapping("/verify/{reference}")
    @ResponseStatus(HttpStatus.OK)
    public TransactionModel verifyTransaction(@PathVariable("reference") String reference) throws ValuePlusException {
        return transferService.verify(getLoggedInUser(), reference);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    public CompletableFuture<Void> verifyPendingTransaction() {
        return transferService.verifyPendingTransactions();
    }

    @GetMapping("/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getTransferByDate(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "status", required = false) String status,
            @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return transferService.filter(getLoggedInUser(), status, startDate, endDate, pageable);
    }

    @GetMapping("/user/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getUserTransferByDate(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(sort = "id", direction = DESC) Pageable pageable) throws ValuePlusException {
        return transferService.getTransactionBetween(getLoggedInUser(), startDate, endDate, pageable);
    }
}
