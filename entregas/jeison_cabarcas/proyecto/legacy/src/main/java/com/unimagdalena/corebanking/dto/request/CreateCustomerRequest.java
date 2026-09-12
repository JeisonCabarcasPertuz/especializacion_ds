package com.unimagdalena.corebanking.dto.request;

import com.unimagdalena.corebanking.enums.DocumentType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCustomerRequest {

	@NotNull
	private DocumentType documentType;

	@NotBlank
	private String documentNumber;

	@NotBlank
	private String fullName;

	@NotBlank
	@Email
	private String email;
}
