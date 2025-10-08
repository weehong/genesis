package com.resetrix.genesis.modules.authentication.controllers;

import com.resetrix.genesis.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignInResponse;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignUpResponse;
import com.resetrix.genesis.modules.authentication.services.IAuthenticationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/authentication")
public class AuthenticationController {

    private final IAuthenticationService service;

    public AuthenticationController(IAuthenticationService service) {
        this.service = service;
    }

    @PostMapping(value = "/sign-in")
    @ResponseStatus(HttpStatus.OK)
    CognitoSignInResponse signIn(@Valid @RequestBody CognitoSignInRequest request) {
        return service.signIn(request);
    }

    @PostMapping(value = "/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    CognitoSignUpResponse signUp(@Valid @RequestBody CognitoSignUpRequest request) {
        return service.signUp(request);
    }
}
