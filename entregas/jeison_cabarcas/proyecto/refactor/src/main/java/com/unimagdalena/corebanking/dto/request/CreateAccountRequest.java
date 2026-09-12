package com.unimagdalena.corebanking.dto.request;

import com.unimagdalena.corebanking.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccountRequest {

	@NotNull
	private AccountType accountType;
}
