package com.jamie.builder.enums;

import java.util.Arrays;


public enum AnsiCode {

    BOLD("1", StyleType.INTENSITY),
    UNDERLINE("4", StyleType.UNDERLINE),
    REVERSED("7", StyleType.FOREGROUND_COLOR),

    BLACK("30", StyleType.FOREGROUND_COLOR),
    RED("31", StyleType.FOREGROUND_COLOR),
    GREEN("32", StyleType.FOREGROUND_COLOR),
    YELLOW("33", StyleType.FOREGROUND_COLOR),
    BLUE("34", StyleType.FOREGROUND_COLOR),
    MAGENTA("35", StyleType.FOREGROUND_COLOR),
    CYAN("36", StyleType.FOREGROUND_COLOR),
    WHITE("37", StyleType.FOREGROUND_COLOR),
    BRIGHT_BLACK("30;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_RED("31;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_GREEN("32;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_YELLOW("33;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_BLUE("34;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_MAGENTA("35;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_CYAN("36;1", StyleType.FOREGROUND_COLOR),
    BRIGHT_WHITE("37;1", StyleType.FOREGROUND_COLOR),

    BACKGROUND_BLACK("40", StyleType.BACKGROUND_COLOR),
    BACKGROUND_RED("41", StyleType.BACKGROUND_COLOR),
    BACKGROUND_GREEN("42", StyleType.BACKGROUND_COLOR),
    BACKGROUND_YELLOW("43", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BLUE("44", StyleType.BACKGROUND_COLOR),
    BACKGROUND_MAGENTA("45", StyleType.BACKGROUND_COLOR),
    BACKGROUND_CYAN("46", StyleType.BACKGROUND_COLOR),
    BACKGROUND_WHITE("47", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_BLACK("40;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_RED("41;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_GREEN("42;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_YELLOW("43;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_BLUE("44;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_MAGENTA("45;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_CYAN("46;1", StyleType.BACKGROUND_COLOR),
    BACKGROUND_BRIGHT_WHITE("47;1", StyleType.BACKGROUND_COLOR),

    UP("\\d+A"),
    DOWN("\\d+B"),
    LEFT("\\d+C"),
    RIGHT("\\d+D"),

    CLEAR_SCREEN_END("0J"),
    CLEAR_SCREEN_START("1J"),
    CLEAR_SCREEN("2J"),

    CLEAR_LINE_END("0K"),
    CLEAR_LINE_START("1K"),
    CLEAR_LINE("2K"),

    RESET("0", StyleType.ALL, true),
    RESET_COLOR_AND_INTENSITY("22", StyleType.INTENSITY, true),
    RESET_FOREGROUND_COLOR("39", StyleType.FOREGROUND_COLOR, true),
    RESET_BACKGROUND_COLOR("49", StyleType.BACKGROUND_COLOR, true);

    private final StyleType styleType;
    private final String code;
    private final boolean removesStyle;
    private final String className;

    AnsiCode(String code) {
        this(code, null, false);
    }

    AnsiCode(String code, StyleType styleType) {
        this(code, styleType, false);
    }

    AnsiCode(String code, StyleType styleType, boolean removesStyle) {
        this.code = code;
        this.removesStyle = removesStyle;
        this.styleType = styleType;
        this.className = this.styleType != null ? this.name().toLowerCase().replaceAll("_", "-") : null;
    }

    public String getDisplayValue() {
        return "\u001b[" + code + 'm';
    }

    public boolean removesStyle() {
        return removesStyle;
    }

    public String getClassName() {
        return className;
    }

    public StyleType getStyleType() {
        return styleType;
    }

    public static AnsiCode find(String code) {
        return Arrays.stream(AnsiCode.values()).filter(c -> code.matches("^" + c.code + "$")).findFirst().orElse(null);
    }
}
