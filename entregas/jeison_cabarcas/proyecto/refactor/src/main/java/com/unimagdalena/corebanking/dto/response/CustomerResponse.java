package com.unimagdalena.corebanking.dto.response;

import com.unimagdalena.corebanking.enums.DocumentType;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CustomerResponse {

	private UUID id;
	private DocumentType documentType;
	private String documentNumber;
	private String fullName;
	private String email;
	private Instant createdAt;
}
