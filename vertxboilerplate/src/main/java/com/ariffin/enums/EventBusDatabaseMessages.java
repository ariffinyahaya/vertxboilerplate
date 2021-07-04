package com.ariffin.enums;

public enum EventBusDatabaseMessages {
    // Simple DB DBQuery
    DBQuery1("DBDBQuery.One"),
    DBQuery2("DBQuery.Two"),
    DBQuery3("DBQuery.Three"),
    DBConfig("DBQuery.Config");

    private final String label;

    private static final int size;
    static {
        size = values().length;
    }

    EventBusDatabaseMessages(String label) {
        this.label = label;
    }

    public static EventBusDatabaseMessages valueOfLabel(String label) {
        for (EventBusDatabaseMessages e : values()) {
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

