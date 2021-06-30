package io.github.jerryshell.fund.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ResultCode {
    SUCCESS(200), ERROR(500);

    private final int value;

    ResultCode(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
