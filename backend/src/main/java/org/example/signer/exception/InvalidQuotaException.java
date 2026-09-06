package org.example.signer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidQuotaException extends RuntimeException {
    public InvalidQuotaException(String message) {
        super(message);
    }
}
