package com.resetrix.genesis.modules.authentication;

import com.resetrix.genesis.modules.authentication.controllers.AuthenticationController;
import com.resetrix.genesis.modules.authentication.exceptions.handlers.AuthenticationExceptionHandler;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignUpRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignInResponse;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignUpResponse;
import com.resetrix.genesis.modules.authentication.services.AuthenticationService;
import com.resetrix.genesis.shared.helpers.JsonFileReader;
import com.resetrix.genesis.testsupports.securities.SecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import({SecurityConfiguration.class, AuthenticationExceptionHandler.class})
@WebMvcTest(value = AuthenticationController.class)
public class SignUpAuthenticationControllerTest {

    private static final String BASE_URL = "/api/v1/authentication";
    private static final String MODULE = "modules/authentication";

    private final MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    public SignUpAuthenticationControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void signUp_shouldAuthenticateUser_whenValidRequestDataProvided()
        throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("valid")
            .readRequestAsString();

        CognitoSignUpResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("success")
            .readResponse(CognitoSignUpResponse.class);

        when(authenticationService.signUp(any(CognitoSignUpRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.data.userSub").value(response.userSub()))
            .andExpect(jsonPath("$.data.codeDeliveryDetails").value(response.codeDeliveryDetails()))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signUp_shouldReturnBadRequestAndErrorMessage_whenMissingFieldsProvided() throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("missing-fields")
            .readRequestAsString();

        String response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("missing-fields")
            .readResponseAsString();

        when(authenticationService.signUp(any(CognitoSignUpRequest.class)))
            .thenThrow(InvalidPasswordException.builder()
                .message(response)
                .build());

        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Multiple validation errors occurred. See 'errors' for details."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-up"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(4))
            .andExpect(jsonPath("$.errors[?(@.field == 'email' && @.message == 'Email is required')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber' && @.message == 'Phone Number is required')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber' && @.message == 'Phone number must be in E.164 format (e.g., +1234567890)')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password is required')]").exists());
    }

    @Test
    void signUp_shouldReturnBadRequestAndErrorMessage_whenEmptyFieldProvided() throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("empty-fields")
            .readRequestAsString();

        String response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("empty-fields")
            .readResponseAsString();

        when(authenticationService.signUp(any(CognitoSignUpRequest.class)))
            .thenThrow(InvalidPasswordException.builder()
                .message(response)
                .build());

        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Multiple validation errors occurred. See 'errors' for details."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-up"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber' && @.message == 'Phone Number is required')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber' && @.message == 'Phone number must be in E.164 format (e.g., +1234567890)')]").exists());
    }

    @Test
    void signUp_shouldReturnBadRequestAndErrorMessage_whenInvalidPasswordPolicyProvided() throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("invalid-password-policy")
            .readRequestAsString();

        String response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("invalid-password-policy")
            .readResponseAsString();

        when(authenticationService.signUp(any(CognitoSignUpRequest.class)))
            .thenThrow(InvalidPasswordException.builder()
                .message(response)
                .build());

        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Invalid Password"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Password did not conform with policy."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-up"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[0].field").value("password"))
            .andExpect(jsonPath("$.errors[0].message", containsString("Password did not conform with policy")));
    }

    @Test
    void signUp_shouldReturnBadRequestAndErrorMessage_whenInvalidPhoneFormatProvided() throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("invalid-phone-format")
            .readRequestAsString();

        String response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-up")
            .scenario("invalid-phone-format")
            .readResponseAsString();

        when(authenticationService.signUp(any(CognitoSignUpRequest.class)))
            .thenThrow(InvalidPasswordException.builder()
                .message(response)
                .build());

        mockMvc.perform(post(BASE_URL + "/sign-up")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Multiple validation errors occurred. See 'errors' for details."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-up"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(1))
            .andExpect(jsonPath("$.errors[?(@.field == 'phoneNumber' && @.message == 'Phone number must be in E.164 format (e.g., +1234567890)')]").exists());
    }
}
