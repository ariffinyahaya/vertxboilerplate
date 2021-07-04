package com.ariffin.enums;

public enum MessageHeaderKeyEnum {
    STATUS("status","\"status\""),
    CAUSE("cause","\"cause\"");

    private final String label;
    private final String jsonLabel;

    private static final int size;
    static {
        size = values().length;
    }

    MessageHeaderKeyEnum(String label, String jsonLabel) {
        this.label = label;
        this.jsonLabel = jsonLabel;
    }

    public static MessageHeaderKeyEnum valueOfLabel(String label) {
        for (MessageHeaderKeyEnum e : values()) {
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
