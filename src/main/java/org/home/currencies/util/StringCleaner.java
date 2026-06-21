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
