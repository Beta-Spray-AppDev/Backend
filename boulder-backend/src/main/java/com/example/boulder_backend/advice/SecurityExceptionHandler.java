package com.example.boulder_backend.advice;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler({
            io.jsonwebtoken.ExpiredJwtException.class,
            io.jsonwebtoken.JwtException.class
    })
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String,String> handleJwt(RuntimeException ex) {
        return Map.of(
                "error", "Unauthorized",
                "message", ex.getMessage() == null ? "Invalid token" : ex.getMessage()
        );
    }
}
