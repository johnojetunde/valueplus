package com.valueplus.app.controller;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.app.model.LoginResponseModel;
import com.valueplus.domain.model.LoginForm;
import com.valueplus.domain.service.abstracts.AuthenticationService;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "v1/auth", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/agent/login")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = LoginResponseModel.class)})
    public LoginResponseModel agentLogin(@Valid @RequestBody LoginForm loginForm) throws ValuePlusException {
        return authenticationService.agentLogin(loginForm);
    }

    @PostMapping("/admin/login")
    @ApiResponses({@ApiResponse(code = 200, message = "Success", response = LoginResponseModel.class)})
    public LoginResponseModel adminLogin(@Valid @RequestBody LoginForm emailModel) throws ValuePlusException {
        return authenticationService.adminLogin(emailModel);
    }
}
