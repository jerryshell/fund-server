package io.github.jerryshell.fund.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result<T> {
    private Boolean success;
    private ResultCode code;
    private String message;
    private T data;

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(true, ResultCode.SUCCESS, message, data);
    }

    public static <T> Result<T> success(String message) {
        return success(message, null);
    }

    public static <T> Result<T> success(T data) {
        return success("success", data);
    }

    public static <T> Result<T> success() {
        return success("success", null);
    }

    public static <T> Result<T> error(String message, T data) {
        return new Result<>(false, ResultCode.ERROR, message, data);
    }

    public static <T> Result<T> error(String message) {
        return error(message, null);
    }

    public static <T> Result<T> error(T data) {
        return error("error", data);
    }

    public static <T> Result<T> error() {
        return error("error", null);
    }
}
