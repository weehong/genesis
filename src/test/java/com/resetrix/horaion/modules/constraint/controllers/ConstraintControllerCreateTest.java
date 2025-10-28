package com.resetrix.horaion.modules.constraint.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.horaion.modules.constraint.enums.SourceType;
import com.resetrix.horaion.modules.constraint.properties.FieldDefinition;
import com.resetrix.horaion.modules.constraint.properties.FieldOption;
import com.resetrix.horaion.modules.constraint.properties.FieldOptions;
import com.resetrix.horaion.modules.constraint.properties.Schema;
import com.resetrix.horaion.modules.constraint.requests.ConstraintRequest;
import com.resetrix.horaion.modules.constraint.responses.ConstraintResponse;
import com.resetrix.horaion.modules.constraint.services.ConstraintService;
import com.resetrix.horaion.testsupports.securities.SecurityConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(SecurityConfiguration.class)
@WebMvcTest(value = ConstraintController.class)
public class ConstraintControllerCreateTest {

    private static final String BASE_URL = "/api/v1/departments/1/constraints";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean
    private ConstraintService constraintService;

    @Autowired
    public ConstraintControllerCreateTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    private ConstraintRequest validRequest;
    private ConstraintResponse validResponse;

    @BeforeEach
    void setUp() {
        UUID testUuid = UUID.randomUUID();

        // Create valid field options
        FieldOptions options = new FieldOptions(
            List.of(new FieldOption("value1", "Label 1")),
            null,
            null
        );

        FieldDefinition fieldDefinition = new FieldDefinition(
            1,
            "test_field",
            "select",
            "Test Field",
            "Select a value",
            options,
            SourceType.SELECT
        );

        Schema schema = new Schema(List.of(fieldDefinition));

        // Create valid request
        validRequest = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            "Test sentence for constraint validation",
            schema
        );

        // Create valid response
        validResponse = new ConstraintResponse(
            1L,
            testUuid,
            "Test Constraint",
            "Test Description",
            "Test sentence for constraint validation",
            schema,
            false,
            Timestamp.from(Instant.now()),
            Timestamp.from(Instant.now())
        );
    }

    @Test
    @WithMockUser
    void create_shouldReturnCreatedConstraint_whenValidRequestProvided() throws Exception {
        when(constraintService.save(any(ConstraintRequest.class))).thenReturn(validResponse);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(validResponse.id()))
            .andExpect(jsonPath("$.name").value(validResponse.name()));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenNameIsMissing() throws Exception {
        ConstraintRequest invalidRequest = new ConstraintRequest(
            null, // missing name
            "Test Description",
            "Test sentence",
            validRequest.schema()
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL))
            .andExpect(jsonPath("$.errors.name").value("Constraint name is required"));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenSentenceIsMissing() throws Exception {
        ConstraintRequest invalidRequest = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            null, // missing sentence
            validRequest.schema()
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL))
            .andExpect(jsonPath("$.errors.sentence").value("Constraint sentence is required"));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenSchemaIsMissing() throws Exception {
        ConstraintRequest invalidRequest = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            "Test sentence",
            null // missing schema
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL))
            .andExpect(jsonPath("$.errors.schema").value("Schemas is required"));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenSchemaFieldsIsEmpty() throws Exception {
        Schema emptySchema = new Schema(List.of()); // empty fields list

        ConstraintRequest invalidRequest = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            "Test sentence",
            emptySchema
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL))
            .andExpect(jsonPath("$.errors['schema.fields']").value("At least one field is required"));
    }

    @Test
    @WithMockUser
    void create_shouldReturnBadRequest_whenFieldDefinitionHasInvalidData() throws Exception {
        FieldDefinition invalidFieldDefinition = new FieldDefinition(
            null, // missing id
            null, // missing name
            null, // missing type
            null, // missing label
            "placeholder",
            null,
            null // missing sourceType
        );

        Schema invalidSchema = new Schema(List.of(invalidFieldDefinition));

        ConstraintRequest invalidRequest = new ConstraintRequest(
            "Test Constraint",
            "Test Description",
            "Test sentence",
            invalidSchema
        );

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.type").value("about:blank"))
            .andExpect(jsonPath("$.title").value("Bad Request"))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.detail").value("Invalid request content."))
            .andExpect(jsonPath("$.instance").value(BASE_URL));
    }

    @Test
    @WithMockUser
    void create_shouldReturnInternalServerError_whenJsonDeserializationFails() throws Exception {
        // This test reproduces the issue from the curl command
        // The JSON has invalid structure that causes deserialization to fail
        String invalidJson = """
            {
                "name": "Shift Demand Requirement",
                "description": "Define how many employees should be assigned to specific shifts",
                "sentence": "There [requirement_type] be [quantity_type] [number_of_employees] employees with roles [roles] assigned to [shifts] shift",
                "schema": {
                    "fields": [
                        {
                            "id": 4,
                            "name": "roles",
                            "type": "multi-select",
                            "label": "Roles",
                            "placeholder": "Select roles",
                            "options": {
                                "option": null,
                                "type": "select"
                            },
                            "sourceType": "DATABASE"
                        }
                    ]
                }
            }
            """;

        // Don't mock the service to see the actual error
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson)
                .with(csrf()))
            .andDo(print())
            .andExpect(status().isInternalServerError());
    }
}
