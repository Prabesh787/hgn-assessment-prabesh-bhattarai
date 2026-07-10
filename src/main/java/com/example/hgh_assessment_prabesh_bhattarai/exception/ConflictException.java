package com.example.hgh_assessment_prabesh_bhattarai.exception;

/** Maps to HTTP 409: the request is well formed but conflicts with current state. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
