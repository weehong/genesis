package com.resetrix.horaion.modules.constraint.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resetrix.horaion.modules.constraint.enums.SourceType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SchemaJsonTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeWithFieldsPropertyName() throws Exception {
        // Given
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

        // When
        String json = objectMapper.writeValueAsString(schema);

        // Then
        assertThat(json).contains("\"fields\":");
        assertThat(json).doesNotContain("\"schemas\":");
    }

    @Test
    void shouldDeserializeFromFieldsPropertyName() throws Exception {
        // Given
        String json = """
            {
                "fields": [
                    {
                        "id": 1,
                        "name": "test_field",
                        "type": "select",
                        "label": "Test Field",
                        "placeholder": "Select a value",
                        "options": {
                            "option": [
                                {
                                    "value": "value1",
                                    "label": "Label 1"
                                }
                            ]
                        },
                        "sourceType": "SELECT"
                    }
                ]
            }
            """;

        // When
        Schema schema = objectMapper.readValue(json, Schema.class);

        // Then
        assertThat(schema.fields()).hasSize(1);
        assertThat(schema.fields().get(0).name()).isEqualTo("test_field");
        assertThat(schema.fields().get(0).type()).isEqualTo("select");
        assertThat(schema.fields().get(0).label()).isEqualTo("Test Field");
    }
}
