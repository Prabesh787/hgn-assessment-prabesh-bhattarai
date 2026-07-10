package com.example.hgh_assessment_prabesh_bhattarai.dto.response;

public record ApiResponse<T>(int statusCode, String message, boolean status, T data) {

    public static <T> ApiResponse<T> success(int statusCode, String message, T data) {
        return new ApiResponse<>(statusCode, message, true, data);
    }

    public static ApiResponse<Object> error(int statusCode, String message) {
        return new ApiResponse<>(statusCode, message, false, null);
    }
}
