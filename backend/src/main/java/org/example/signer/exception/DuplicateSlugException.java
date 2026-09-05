package org.example.signer.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateSlugException extends RuntimeException {
    public DuplicateSlugException(String message) {
        super(message);
    }

    public DuplicateSlugException(String slug, Throwable cause) {
        super("Slug already exists: " + slug, cause);
    }
}
