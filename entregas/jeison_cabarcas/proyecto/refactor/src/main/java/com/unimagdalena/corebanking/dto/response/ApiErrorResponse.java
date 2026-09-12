package com.unimagdalena.corebanking.dto.response;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiErrorResponse {

	private Instant timestamp;
	private int status;
	private String error;
	private String code;
	private String message;
	private String path;
	private List<FieldValidationError> details;
}
