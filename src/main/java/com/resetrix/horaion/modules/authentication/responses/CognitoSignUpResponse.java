package com.resetrix.horaion.modules.authentication.responses;

public record CognitoSignUpResponse(
    String userSub,
    String codeDeliveryDetails
) {
}
