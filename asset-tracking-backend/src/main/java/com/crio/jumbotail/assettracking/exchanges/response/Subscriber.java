package com.crio.jumbotail.assettracking.exchanges.response;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


public final class Subscriber extends SseEmitter {
	public Subscriber() {
		super(Long.MAX_VALUE);
	}
}
