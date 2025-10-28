package com.resetrix.horaion.modules.authentication.services;

import com.resetrix.horaion.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.horaion.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.horaion.modules.authentication.responses.CognitoSignInResponse;
import com.resetrix.horaion.modules.authentication.responses.CognitoSignUpResponse;

public sealed interface IAuthenticationService permits AuthenticationService {
    CognitoSignUpResponse signUp(CognitoSignUpRequest request);

    CognitoSignInResponse signIn(CognitoSignInRequest request);
}
