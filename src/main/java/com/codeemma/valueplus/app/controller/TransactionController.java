package com.codeemma.valueplus.app.controller;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.dto.TransactionModel;
import com.codeemma.valueplus.domain.service.abstracts.TransferService;
import com.codeemma.valueplus.domain.util.UserUtils;
import com.codeemma.valueplus.persistence.entity.User;
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
        User loggedInUser = UserUtils.getLoggedInUser();

        return transferService.transfer(loggedInUser, request);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getAllTransfers(@PageableDefault Pageable pageable) throws ValuePlusException {
        return transferService.getAllTransactions(pageable);
    }

    @GetMapping("/user")
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getUserTransfers(@PageableDefault Pageable pageable) throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();
        return transferService.getAllUserTransactions(loggedInUser, pageable);
    }

    @GetMapping("/user/reference/{reference}")
    @ResponseStatus(HttpStatus.OK)
    public Optional<TransactionModel> getUserTransfers(@PathVariable("reference") String reference) throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();
        return transferService.getTransactionByReference(loggedInUser, reference);
    }

    @GetMapping("/user/filter")
    @ResponseStatus(HttpStatus.OK)
    public Page<TransactionModel> getUserTransferByDate(
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault Pageable pageable) throws ValuePlusException {
        User loggedInUser = UserUtils.getLoggedInUser();

        return transferService.getTransactionBetween(loggedInUser, startDate, endDate, pageable);
    }
}
