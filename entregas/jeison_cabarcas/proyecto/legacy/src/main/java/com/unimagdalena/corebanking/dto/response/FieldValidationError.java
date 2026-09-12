package com.unimagdalena.corebanking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class FieldValidationError {

	private String field;
	private String message;
}
