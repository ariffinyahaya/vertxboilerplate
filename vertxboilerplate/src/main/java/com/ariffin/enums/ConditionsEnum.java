package com.ariffin.enums;

public enum ConditionsEnum {
    bloodsugar("bloodsugar", "BLOOD SUGAR \"ROLLER COASTER\""),
    cortisol("cortisol", "CORTISOL DYSREGULATION"),
    lowsex("lowsex", "LOW FREE SEX HORMONES"),
    disleep("disleep", "DISORDERED SLEEP"),
    focus("focus", "FOCUS ISSUES"),
    serotonin("serotonin", "SEROTONIN DISORDER");

    private final String dbCollumn;
    private final String conditionLabel;

    private static final int size;
    static {
        size = values().length;
    }

    ConditionsEnum(String dbCollumn, String conditionLabel) {
        this.dbCollumn = dbCollumn;
        this.conditionLabel = conditionLabel;
    }

    public static ConditionsEnum valueOfdbCollumn(String dbCollumn) {
        for (ConditionsEnum e : values()) {
            if (e.dbCollumn.equals(dbCollumn)) {
                return e;
            }
        }
        return null;
    }

    public static ConditionsEnum valueOfConditionLabel(String conditionLabel) {
        for (ConditionsEnum e : values()) {
            if (e.conditionLabel.equals(conditionLabel)) {
                return e;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return conditionLabel;
    }

    public String dbColumn(){
        return dbCollumn;
    }

    public static int size() {
        return size;
    }
}