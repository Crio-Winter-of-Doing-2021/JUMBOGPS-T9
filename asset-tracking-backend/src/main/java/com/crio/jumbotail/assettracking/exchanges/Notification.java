package com.crio.jumbotail.assettracking.exchanges;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Notification {

	private Long assetId;

	private String message;

	private String eventType;


}
