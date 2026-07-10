package com.example.hgh_assessment_prabesh_bhattarai.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException device(Long id) {
        return new NotFoundException("Device " + id + " not found");
    }

    public static NotFoundException order(Long id) {
        return new NotFoundException("Order " + id + " not found");
    }
}
