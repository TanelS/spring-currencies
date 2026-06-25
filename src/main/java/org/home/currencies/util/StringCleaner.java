package org.home.currencies.util;

import org.apache.commons.text.StringEscapeUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringCleaner {

    private static final Pattern CARRIAGE_RETURN_PATTERN = Pattern.compile("\\r+");
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("[\\n\\t]+");
    private static final Pattern ZERO_WIDTH_PATTERN = Pattern.compile("[\\u200b-\\u200f\\u202a-\\u202e\\ufeff]");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s{2,}");
    private static final Pattern CONTROL_CHARS_PATTERN = Pattern.compile("[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f\\x7f]");


    /**
     * Cleans the input string by applying various transformations to remove unwanted characters
     * and normalize the content. The method performs the following operations in sequence:
     * unescapes HTML entities, normalizes text to NFC form, removes control characters, replaces
     * carriage returns, collapses whitespace, removes zero-width characters, and trims and
     * collapses multiple spaces into a single space. If the cleaned string is blank, it returns null.
     *
     * @param input the input string to be cleaned
     * @return the cleaned and normalized string, or null if the resulting string is blank
     */
    public static String cleanString(String input) {

        if (input.isBlank()) {
            return null;
        }

        input = StringEscapeUtils.unescapeHtml4(input);
        input = java.text.Normalizer.normalize(input, Normalizer.Form.NFKC);
        input = CONTROL_CHARS_PATTERN.matcher(input).replaceAll("");
        input = CARRIAGE_RETURN_PATTERN.matcher(input).replaceAll("");
        input = WHITESPACE_PATTERN.matcher(input).replaceAll(" ");
        input = ZERO_WIDTH_PATTERN.matcher(input).replaceAll("");
        input = MULTIPLE_SPACES_PATTERN.matcher(input).replaceAll(" ");
        input = input.strip();

        return input.isBlank() ? null : input;
    }

    /**
     * Cleans and normalizes all string values within the given JSON tree. For each string value,
     * it applies the defined cleaning rules, which include removing control characters, collapsing
     * whitespace, and handling zero-width and special characters. Non-string values are left
     * unchanged. If a string value becomes blank after cleaning, it is replaced with null.
     *
     * @param node the JSON tree to clean; it can be an object, array, or a single value
     * @return the cleaned JSON tree with updated string values
     */
    public static JsonNode cleanTree(JsonNode node) {

        if (node.isObject()) {
            for (var entry : node.properties()) {
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (value.isString()) {
                    String cleaned = cleanString(value.asString());
                    ((ObjectNode) node).set(key, cleaned == null ? null : StringNode.valueOf(cleaned));
                } else {
                    cleanTree(value);
                }
            }
        } else if (node.isArray()) {
            for (int i = 0; i < node.size(); i++) {
                JsonNode value = node.get(i);
                if (value.isString()) {
                    String cleaned = cleanString(value.asString());
                    ((ArrayNode) node).set(i, cleaned == null ? null : StringNode.valueOf(cleaned));
                } else {
                    cleanTree(value);
                }
            }
        } else if (node.isString()) {
            String cleanedString = cleanString(node.asString());
            return cleanedString == null ? null : StringNode.valueOf(cleanedString);

        }
        return node;
    }
}
