package com.resetrix.genesis.modules.authentication.responses;

public record CognitoSignUpResponse(
    String userSub,
    String codeDeliveryDetails
) {
}
