package com.gustavosdaniel.stock_flow_api.exception.handle;

import com.gustavosdaniel.stock_flow_api.exception.InsufficientStockException;
import com.gustavosdaniel.stock_flow_api.exception.InvalidQuantityException;
import com.gustavosdaniel.stock_flow_api.exception.UnauthorizedException;
import com.gustavosdaniel.stock_flow_api.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status, ProblemType type, String detail){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setType(type.getUri());
        problemDetail.setTitle(type.getTitle());
        problemDetail.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ProblemType.INTERNAL_ERROR,
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ProblemDetail> handleValidation(WebExchangeBindException exception){

        log.warn("Validação falhou {}", exception.getMessage());

        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult().getFieldErrors().forEach(fieldError -> {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        });

        ResponseEntity<ProblemDetail> response = buildResponse(
                HttpStatus.BAD_REQUEST,
                ProblemType.VALIDATE_ERROR,
                "Erro de validação nos campos"
        );

        assert response.getBody() != null;
        response.getBody().setProperty("fieldsErrors", errors);

        return response;
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleUserNotFound(
            UserNotFoundException exception){

        log.warn("Usuário não encontrado {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ProblemType.USER_NOT_FOUND,
                exception.getMessage()
        );

    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN,
                ProblemType.ACCESS_DENIED, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(UnauthorizedException exception){

        log.warn("O usuário não tem autorização para realizar essa operação {}",
                exception.getMessage());

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ProblemType.UNAUTHORIZED,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ProblemDetail> handleInsufficientStock(
            InsufficientStockException exception){

        log.warn("Estoque insuficiente para realizar essa operação {}",
                exception.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ProblemType.INSUFFICIENT_STOCK,
                exception.getMessage()
        );
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ResponseEntity<ProblemDetail> handleInvalidQuantity(
            InvalidQuantityException exception){

        log.warn("Quantidade inválida: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ProblemType.INVALID_QUANTITY,
                exception.getMessage()
        );
    }
}
