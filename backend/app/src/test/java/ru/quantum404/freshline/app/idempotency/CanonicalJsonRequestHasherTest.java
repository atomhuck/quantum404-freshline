package ru.quantum404.freshline.app.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CanonicalJsonRequestHasherTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CanonicalJsonRequestHasher hasher = new CanonicalJsonRequestHasher(objectMapper);

    @Test
    void ignoresWhitespaceAndObjectFieldOrderRecursively() throws Exception {
        JsonNode first = objectMapper.readTree("""
                {"supplier":"North Farm","lines":[{"ingredientId":"42","quantity":"1.000"}]}
                """);
        JsonNode sameRequest = objectMapper.readTree("""
                { "lines" : [ { "quantity" : "1.000", "ingredientId" : "42" } ], "supplier" : "North Farm" }
                """);

        assertEquals(hasher.sha256(first), hasher.sha256(sameRequest));
    }

    @Test
    void preservesArrayOrderAndDifferentValues() throws Exception {
        JsonNode first = objectMapper.readTree("""
                {"lines":[{"ingredientId":"a"},{"ingredientId":"b"}]}
                """);
        JsonNode reversed = objectMapper.readTree("""
                {"lines":[{"ingredientId":"b"},{"ingredientId":"a"}]}
                """);
        JsonNode changed = objectMapper.readTree("""
                {"lines":[{"ingredientId":"a"},{"ingredientId":"c"}]}
                """);

        assertNotEquals(hasher.sha256(first), hasher.sha256(reversed));
        assertNotEquals(hasher.sha256(first), hasher.sha256(changed));
    }

    @Test
    void returnsLowercaseSha256Hex() throws Exception {
        JsonNode body = objectMapper.readTree("{\"deliveryNumber\":\"D-1\"}");

        assertTrue(hasher.sha256(body).matches("[0-9a-f]{64}"));
    }

    @Test
    void rejectsNullBody() {
        assertThrows(IllegalArgumentException.class, () -> hasher.sha256(null));
    }
}
