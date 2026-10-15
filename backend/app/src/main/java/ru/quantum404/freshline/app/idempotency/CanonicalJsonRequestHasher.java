package ru.quantum404.freshline.app.idempotency;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/** Computes a stable body hash for idempotency comparisons. */
@Component
public final class CanonicalJsonRequestHasher {
    private final ObjectMapper objectMapper;

    public CanonicalJsonRequestHasher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Hashes compact JSON with object keys sorted recursively. Array order is
     * preserved because array positions are part of the submitted request.
     */
    public String sha256(JsonNode requestBody) {
        if (requestBody == null) {
            throw new IllegalArgumentException("requestBody must not be null");
        }

        try {
            byte[] canonicalJson = objectMapper.writeValueAsBytes(canonicalize(requestBody));
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(canonicalJson);
            return HexFormat.of().formatHex(digest);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("requestBody cannot be serialized as JSON", exception);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private JsonNode canonicalize(JsonNode node) {
        if (node.isObject()) {
            ObjectNode sorted = JsonNodeFactory.instance.objectNode();
            List<String> fieldNames = new ArrayList<>();
            node.fieldNames().forEachRemaining(fieldNames::add);
            fieldNames.sort(String::compareTo);
            for (String fieldName : fieldNames) {
                sorted.set(fieldName, canonicalize(node.get(fieldName)));
            }
            return sorted;
        }

        if (node.isArray()) {
            ArrayNode ordered = JsonNodeFactory.instance.arrayNode();
            node.forEach(value -> ordered.add(canonicalize(value)));
            return ordered;
        }

        return node;
    }
}
