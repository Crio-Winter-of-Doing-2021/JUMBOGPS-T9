package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.response.Notification;
import com.crio.jumbotail.assettracking.exchanges.response.Subscriber;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@Service
@Log4j2
public class SubscriptionServiceImpl implements SubscriptionService {

	private final CopyOnWriteArrayList<Subscriber> subscribers = new CopyOnWriteArrayList<>();

	@Override
	public Subscriber subscribe(Subscriber subscriber) {
		subscriber.onTimeout(() -> this.subscribers.remove(subscriber));

		this.subscribers.add(subscriber);

		LOG.info("Total Subscriber {}" , subscribers.size());

		return subscriber;
	}

	@EventListener
	@Override
	public void notifySubscriber(Notification notification) {
		List<Subscriber> deadSubscribers = new ArrayList<>();
		for (Subscriber subscriber : this.subscribers) {
			try {
				LOG.info("Notification Data {}", notification);

				SseEventBuilder sseEventBuilder = SseEmitter.event()
						.id(LocalDateTime.now().toString())
						.data(notification, MediaType.APPLICATION_JSON)
						.reconnectTime(10_000L);

				subscriber.send(sseEventBuilder.build());

			} catch (IOException e) {
				LOG.info("Failed To Notify ", e);
				subscriber.onError(error -> {
					LOG.info("subscriber dropped out. Removing from list");
					subscriber.completeWithError(error);
				});
				deadSubscribers.add(subscriber);
			}
		}

		LOG.info("notification done");

		if(!deadSubscribers.isEmpty()) {
			this.subscribers.removeAll(deadSubscribers);
			LOG.info("Removed Dead Subscribers");
		}
		LOG.info("Total Subscriber {}" , subscribers.size());
	}
}
