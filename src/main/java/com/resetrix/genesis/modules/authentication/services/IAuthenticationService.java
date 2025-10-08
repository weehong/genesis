package com.resetrix.genesis.modules.authentication.services;

import com.resetrix.genesis.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignInResponse;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignUpResponse;

public sealed interface IAuthenticationService permits AuthenticationService {
    CognitoSignUpResponse signUp(CognitoSignUpRequest request);

    CognitoSignInResponse signIn(CognitoSignInRequest request);
}
