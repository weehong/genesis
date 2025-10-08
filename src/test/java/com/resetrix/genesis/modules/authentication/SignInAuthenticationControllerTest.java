package com.resetrix.genesis.modules.authentication;

import com.resetrix.genesis.modules.authentication.controllers.AuthenticationController;
import com.resetrix.genesis.modules.authentication.requests.CognitoSignInRequest;
import com.resetrix.genesis.modules.authentication.responses.CognitoSignInResponse;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = AuthenticationController.class)
public class SignInAuthenticationControllerTest {

    private static final String BASE_URL = "/api/v1/authentication";
    private static final String MODULE = "modules/authentication";

    private final MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    public SignInAuthenticationControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @Test
    void signIn_shouldAuthenticateUser_whenValidUserAndPasswordProvided()
        throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-in")
            .scenario("valid")
            .readRequestAsString();

        CognitoSignInResponse response = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-in")
            .scenario("success")
            .readResponse(CognitoSignInResponse.class);

        when(authenticationService.signIn(any(CognitoSignInRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post(BASE_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").doesNotExist())
            .andExpect(jsonPath("$.data.accessToken").value(response.accessToken()))
            .andExpect(jsonPath("$.data.refreshToken").value(response.refreshToken()))
            .andExpect(jsonPath("$.data.idToken").value(response.idToken()))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void signIn_shouldReturnValidationErrors_whenFieldsAreMissing()
        throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-in")
            .scenario("missing-fields")
            .readRequestAsString();

        mockMvc.perform(post(BASE_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Multiple validation errors occurred. See 'errors' for details."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-in"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(2))
            .andExpect(jsonPath("$.errors[?(@.field == 'email' && @.message == 'Email is required.')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password is required.')]").exists());
    }

    @Test
    void signIn_shouldReturnValidationErrors_whenFieldsAreEmpty()
        throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-in")
            .scenario("empty-fields")
            .readRequestAsString();

        mockMvc.perform(post(BASE_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Multiple validation errors occurred. See 'errors' for details."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-in"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(4))
            .andExpect(jsonPath("$.errors[?(@.field == 'email' && @.message == 'Email is required.')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password is required.')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password must be between 8 and 128 characters.')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.')]").exists());
    }

    @Test
    void signIn_shouldReturnValidationErrors_whenDataIsInvalid()
        throws Exception {
        String request = JsonFileReader.builder()
            .module(MODULE)
            .endpoint("sign-in")
            .scenario("invalid-data")
            .readRequestAsString();

        mockMvc.perform(post(BASE_URL + "/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Validation Failed"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Multiple validation errors occurred. See 'errors' for details."))
            .andExpect(jsonPath("$.instance").value("/api/v1/authentication/sign-in"))
            .andExpect(jsonPath("$.errors").isArray())
            .andExpect(jsonPath("$.errors.length()").value(3))
            .andExpect(jsonPath("$.errors[?(@.field == 'email' && @.message == 'Email must be valid.')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password must be between 8 and 128 characters.')]").exists())
            .andExpect(jsonPath("$.errors[?(@.field == 'password' && @.message == 'Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.')]").exists());
    }
}
