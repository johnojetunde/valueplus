package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.service.abstracts.BankService;
import com.valueplus.paystack.model.BankModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(path = "v1/banks", produces = MediaType.APPLICATION_JSON_VALUE)
public class BanksController {

    private final BankService bankService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<BankModel> getAll() throws ValuePlusException {
        return bankService.getBanks();
    }
}
