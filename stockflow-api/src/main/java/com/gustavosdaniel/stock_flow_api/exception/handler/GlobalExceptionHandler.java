package com.gustavosdaniel.stock_flow_api.exception.handler;

import com.gustavosdaniel.stock_flow_api.exception.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handler for the entire REST API.
 * <p>
 * Intercepts exceptions thrown by any controller and translates them into
 * RFC 7807 {@link ProblemDetail} responses with appropriate HTTP status codes,
 * problem-type URIs, and human-readable titles. Handles both domain-specific
 * exceptions (not-found, business-rule violations, concurrency conflicts) and
 * framework-level exceptions (validation, access-denied, circuit-breaker).
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Builds a standardized {@link ProblemDetail} response body.
     *
     * @param status the HTTP status code
     * @param type   the problem type categorization
     * @param detail a human-readable explanation of the error
     * @return a {@link ResponseEntity} wrapping the problem detail
     */
    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status, ProblemType type, String detail){

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);

        problemDetail.setType(type.getUri());
        problemDetail.setTitle(type.getTitle());
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(status).body(problemDetail);
    }

    /**
     * Handles domain business-rule violations.
     *
     * @param exception the thrown exception
     * @return an HTTP 422 response with the {@code BUSINESS_RULE} problem type
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ProblemDetail> handleBusinessRule(BusinessRuleException exception){

        log.warn("Regra de negócio violada: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.UNPROCESSABLE_CONTENT,
                ProblemType.BUSINESS_RULE,
                exception.getMessage()
        );
    }

    /**
     * Catch-all handler for any unhandled exception, serving as a safety net.
     *
     * @param ex the unhandled exception
     * @return an HTTP 500 response with the {@code INTERNAL_ERROR} problem type
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex) {
        log.error("Erro inesperado: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                ProblemType.INTERNAL_ERROR,
                "Ocorreu um erro inesperado. Tente novamente mais tarde.");
    }

    /**
     * Handles bean validation errors (e.g., {@code @Valid} failures on request bodies).
     * <p>
     * Collects all field-level errors into a {@code fieldsErrors} property
     * on the problem detail response.
     * </p>
     *
     * @param exception the validation exception
     * @return an HTTP 400 response with the {@code VALIDATE_ERROR} problem type
     */
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

        response.getBody().setProperty("fieldsErrors", errors);

        return response;
    }

    /**
     * Handles duplicate-name conflicts when creating or updating entities.
     *
     * @param exception the thrown exception
     * @return an HTTP 400 response with the {@code NAME_EXIST} problem type
     */
    @ExceptionHandler(NameExistException.class)
    public ResponseEntity<ProblemDetail> handleNameExist(NameExistException exception){

        log.warn("O nome inserido já esta em uso {}", exception.getMessage());

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                ProblemType.NAME_EXIST,
                exception.getMessage()
        );
    }

    /**
     * Handles user-not-found lookup failures.
     *
     * @param exception the thrown exception
     * @return an HTTP 404 response with the {@code USER_NOT_FOUND} problem type
     */
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

    /**
     * Handles Spring Security access-denied errors (e.g., insufficient role).
     *
     * @param ex the access-denied exception
     * @return an HTTP 403 response with the {@code ACCESS_DENIED} problem type
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Acesso negado: {}", ex.getMessage());
        return buildResponse(HttpStatus.FORBIDDEN,
                ProblemType.ACCESS_DENIED, ex.getMessage());
    }

    /**
     * Handles authorization failures where the user lacks the required permissions.
     *
     * @param exception the thrown exception
     * @return an HTTP 403 response with the {@code UNAUTHORIZED} problem type
     */
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

    /**
     * Handles insufficient-stock errors when a movement would result in negative inventory.
     *
     * @param exception the thrown exception
     * @return an HTTP 400 response with the {@code INSUFFICIENT_STOCK} problem type
     */
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

    /**
     * Handles invalid-quantity errors (negative, zero, or exceeding maximum thresholds).
     *
     * @param exception the thrown exception
     * @return an HTTP 400 response with the {@code INVALID_QUANTITY} problem type
     */
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

    /**
     * Handles category-not-found lookup failures.
     *
     * @param exception the thrown exception
     * @return an HTTP 404 response with the {@code CATEGORY_NOT_FOUND} problem type
     */
    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleCategoryNotFound(CategoryNotFoundException exception){

        log.warn("Categoria não encontrada {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ProblemType.CATEGORY_NOT_FOUND,
                exception.getMessage()
        );
    }

    /**
     * Handles supplier-not-found lookup failures.
     *
     * @param exception the thrown exception
     * @return an HTTP 404 response with the {@code SUPPLIER_NOT_FOUND} problem type
     */
    @ExceptionHandler(SupplierNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleSupplierNotFound(SupplierNotFoundException exception){

        log.warn("Fornecedor não encontrado {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ProblemType.SUPPLIER_NOT_FOUND,
                exception.getMessage()
        );
    }

    /**
     * Handles circuit-breaker rejections when an external service (e.g., ViaCEP) is
     * temporarily unavailable.
     *
     * @param exception the circuit-breaker exception
     * @return an HTTP 503 response with the {@code EXTERNAL_SERVICE} problem type
     */
    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ProblemDetail> handleCircuitBreaker(CallNotPermittedException exception){

        log.warn("CircuitBreaker interceptou requisição: O serviço ViaCEP está fora do ar.");

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                ProblemType.EXTERNAL_SERVICE,
                exception.getMessage()
        );
    }

    /**
     * Handles product-not-found lookup failures.
     *
     * @param exception the thrown exception
     * @return an HTTP 404 response with the {@code PRODUCT_NOT_FOUND} problem type
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleProductNotFound(ProductNotFoundException exception){

        log.warn("Produto não encontrado {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ProblemType.PRODUCT_NOT_FOUND,
                exception.getMessage()
        );
    }

    /**
     * Handles stock-record-not-found lookup failures.
     *
     * @param exception the thrown exception
     * @return an HTTP 404 response with the {@code STOCK_NOT_FOUND} problem type
     */
    @ExceptionHandler(StockNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleStockNotFound(StockNotFoundException exception){

        log.warn("Estoque não encontrado {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ProblemType.STOCK_NOT_FOUND,
                exception.getMessage()
        );
    }

    /**
     * Handles notification-not-found lookup failures.
     *
     * @param exception the thrown exception
     * @return an HTTP 404 response with the {@code NOTIFICATION_NOT_FOUND} problem type
     */
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotificationNotFound(NotificationNotFoundException exception){

        log.warn("Notificação não encontrada {}", exception.getMessage());

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ProblemType.NOTIFICATION_NOT_FOUND,
                exception.getMessage()
        );
    }

    /**
     * Handles optimistic-locking conflicts when a record was modified by another
     * concurrent request between read and write.
     *
     * @param exception the concurrency exception
     * @return an HTTP 409 response with the {@code CONCURRENCY_CONFLICT} problem type
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticLock(OptimisticLockingFailureException exception){

        log.warn("Conflito de concorrência detectado: {}", exception.getMessage());

        return buildResponse(
                HttpStatus.CONFLICT,
                ProblemType.CONCURRENCY_CONFLICT,
                "Registro modificado por outro usuário. Recarregue os dados e tente novamente."
        );
    }
}
