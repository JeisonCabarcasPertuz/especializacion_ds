package com.unimagdalena.corebanking.exception;

import com.unimagdalena.corebanking.dto.response.ApiErrorResponse;
import com.unimagdalena.corebanking.dto.response.FieldValidationError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<FieldValidationError> details = ex.getBindingResult().getFieldErrors().stream()
				.map(this::toFieldValidationError)
				.toList();
		return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request contains invalid fields", request, details);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, ex.getCode(), ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex, HttpServletRequest request) {
		return build(ex.getStatus(), ex.getCode(), ex.getMessage(), request, List.of());
	}

	@ExceptionHandler(OptimisticLockingFailureException.class)
	public ResponseEntity<ApiErrorResponse> handleOptimisticLock(OptimisticLockingFailureException ex, HttpServletRequest request) {
		return build(HttpStatus.CONFLICT, "ACCOUNT_CONCURRENT_UPDATE",
				"The account was modified concurrently, retry the operation", request, List.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex, HttpServletRequest request) {
		log.error("unexpected error handling request {}", request.getRequestURI(), ex);
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected server error", request, List.of());
	}

	private FieldValidationError toFieldValidationError(FieldError fieldError) {
		return FieldValidationError.builder()
				.field(fieldError.getField())
				.message(fieldError.getDefaultMessage())
				.build();
	}

	private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message,
			HttpServletRequest request, List<FieldValidationError> details) {
		ApiErrorResponse body = ApiErrorResponse.builder()
				.timestamp(Instant.now())
				.status(status.value())
				.error(status.getReasonPhrase())
				.code(code)
				.message(message)
				.path(request.getRequestURI())
				.details(details)
				.build();
		return ResponseEntity.status(status).body(body);
	}
}
