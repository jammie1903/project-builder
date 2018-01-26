package com.jamie.builder.data;

import com.jamie.builder.enums.AnsiCode;
import com.jamie.builder.enums.StyleType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Text;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConsoleLogBuilder {

    private static final String SPECIAL_CHARACTER_REGEX = "(\\u001b\\[(.+?)m)|(\n)";

    private ObservableList<Text> text = FXCollections.observableArrayList();
    private Text currentText;

    private Map<StyleType, String> currentClasses = new HashMap<>();

    public ConsoleLogBuilder() {
        newTextBlock();
    }

    public void append(String newText) {
        Pattern pattern = Pattern.compile(SPECIAL_CHARACTER_REGEX);

        Matcher matcher = pattern.matcher(newText);
        int position = 0;
        while (matcher.find()) {
            appendText(newText.substring(position, matcher.start()));
            boolean newLine = matcher.group(3) != null;
            if (newLine) {
                appendText(matcher.group());
                clearTextClasses(StyleType.ALL);
            } else {
                String codeString = matcher.group(2);
                AnsiCode code = AnsiCode.find(codeString);
                if (code == null) {
                    System.out.println("Code " + codeString + " not handled");
                } else {
                    if (code.removesStyle()) {
                        clearTextClasses(code.getStyleType());
                    } else if (code.getStyleType() != null) {
                        addTextClass(code);
                    } else {
                        System.out.println(code + "occured: " + codeString);
                    }
                }
            }
            position = matcher.end();
        }
        appendText(newText.substring(position));
    }

    public ObservableList<Text> getText() {
        return text;
    }

    private void appendText(String text) {
        currentText.setText(currentText.getText() + text);
    }

    private void addTextClass(AnsiCode code) {
        this.currentClasses.put(code.getStyleType(), code.getClassName());
        newTextBlock();
    }

    private void clearTextClasses(StyleType type) {
        if (type == StyleType.ALL) {
            this.currentClasses.clear();
        } else {
            this.currentClasses.remove(type);
        }
        newTextBlock();
    }

    private void newTextBlock() {
        if (currentText != null && currentText.getText().isEmpty()) {
            currentText.getStyleClass().clear();
            currentText.getStyleClass().add("console-text");
            currentText.getStyleClass().addAll(currentClasses.values());
        } else {
            currentText = new Text();
            currentText.getStyleClass().add("console-text");
            currentText.getStyleClass().addAll(currentClasses.values());
            text.add(currentText);
        }
    }
}
