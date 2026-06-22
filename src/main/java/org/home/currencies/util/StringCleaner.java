package org.home.currencies.util;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

public class StringCleaner {

    public static String cleanString(String input) {
        // TODO add cleaning logic here later
        return input;
    }

    /**
     * Recursively cleans string values in a JSON tree structure by applying the cleaning logic
     * defined in the {@code cleanString} method.
     *
     * If the provided node is an object, it iterates through its properties and cleans any
     * string values. If the node is an array, it iterates through its elements and cleans
     * string values. If the node is a string, it applies the cleaning logic directly.
     *
     * @param node the root of the JSON tree to be cleaned. It can be an object, array, or a string node.
     * @return the cleaned JSON tree structure with string values processed.
     */
    public static JsonNode cleanTree(JsonNode node) {

        if (node.isObject()) {
            for (var entry : node.properties()) {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isString()) {
                    String cleaned = cleanString(value.asString());
                    ((ObjectNode) node).set(key, StringNode.valueOf(cleaned));
                } else {
                    cleanTree(value);
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode value = node.get(i);
                if (value.isString()) {
                    String cleaned = cleanString(value.asString());
                    ((ArrayNode) node).set(i, StringNode.valueOf(cleaned));
                } else {
                    cleanTree(value);
                }
            }
        } else if (node.isString()) {
            return StringNode.valueOf(cleanString(node.asString()));

        }
        return node;
    }
}
