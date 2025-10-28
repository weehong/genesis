package com.resetrix.horaion.shared.helpers;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.resetrix.horaion.modules.constraint.properties.Schema;

/**
 * Utility class for computing and comparing schema hashes.
 * Uses SHA-256 hashing for consistent schema comparison.
 */
public final class HashHelper {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    private HashHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Computes a SHA-256 hash of the provided ConstraintSchema.
     * Serializes the schema to JSON for deterministic hashing.
     *
     * @param schema the schema to hash
     * @return the SHA-256 hash as a hexadecimal string
     * @throws RuntimeException if JSON serialization fails
     */
    public static String computeHash(Schema schema) {
        if (schema == null) {
            return DigestUtils.sha256Hex("");
        }

        try {
            String jsonString = OBJECT_MAPPER.writeValueAsString(schema);
            return DigestUtils.sha256Hex(jsonString);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to compute hash for schema", e);
        }
    }

    /**
     * Compares two constraint schemas by computing their hashes and checking equality.
     *
     * @param schemaA the first schema
     * @param schemaB the second schema
     * @return true if both schemas have the same hash, false otherwise
     */
    public static boolean isSameSchema(Schema schemaA, Schema schemaB) {
        // Handle null cases
        if (schemaA == null && schemaB == null) {
            return true;
        }

        if (schemaA == null || schemaB == null) {
            return false;
        }

        String hashA = computeHash(schemaA);
        String hashB = computeHash(schemaB);

        return hashA.equals(hashB);
    }
}