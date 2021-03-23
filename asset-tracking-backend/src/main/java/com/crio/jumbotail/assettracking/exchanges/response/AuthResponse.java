package com.crio.jumbotail.assettracking.exchanges.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Schema(description = "Response to the Authentication request")
public class AuthResponse {

	@Schema(description = "The JWT token")
	private String token;
}
