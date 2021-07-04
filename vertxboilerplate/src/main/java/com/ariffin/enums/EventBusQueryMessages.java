package com.ariffin.enums;

public enum EventBusQueryMessages {
    // Simple DB Query
    Query1("Query.One"),
    Query2("Query.Two"),
    Query3("Query.Three"),
    Config("Query.Config");

    private final String label;

    private static final int size;
    static {
        size = values().length;
    }

    EventBusQueryMessages(String label) {
        this.label = label;
    }

    public static EventBusQueryMessages valueOfLabel(String label) {
        for (EventBusQueryMessages e : values()) {
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

    public static int size() {
        return size;
    }
}

