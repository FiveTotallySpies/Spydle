package dev.totallyspies.spydle.matchmaker.config;

import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ClientErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        StringBuilder builder = new StringBuilder();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                builder.append(error.getField()).append(": ").append(error.getDefaultMessage()).append(", "));
        if (builder.length() >= 2) {
            builder.delete(builder.length() - 2, builder.length());
        }
        return ResponseEntity.status(400).body(new ClientErrorResponse().message(builder.toString()));
    }

}