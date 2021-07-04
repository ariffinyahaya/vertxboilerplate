package com.ariffin.enums;

public enum MessageHeaderStatusValueEnum {
    ERROR("1","\"1\""),
    SUCCESS("0","\"0\"");

    private final String label;
    private final String jsonLabel;

    private static final int size;
    static {
        size = values().length;
    }

    MessageHeaderStatusValueEnum(String label, String jsonLabel) {
        this.label = label;
        this.jsonLabel = jsonLabel;
    }

    public static MessageHeaderStatusValueEnum valueOfLabel(String label) {
        for (MessageHeaderStatusValueEnum e : values()) {
            if (e.label.equals(label)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return label;
    }

    public String toJsonString() {
        return jsonLabel;
    }
    public static int size() {
        return size;
    }
}
