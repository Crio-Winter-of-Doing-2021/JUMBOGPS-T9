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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@Service
@Log4j2
public class SubscriptionServiceImpl implements SubscriptionService {

	// to avoid concurrent modification exceptions
	// if new subscribers are registered while notifications are being sent
	private final CopyOnWriteArrayList<Subscriber> subscribers = new CopyOnWriteArrayList<>();

	@Override
	public Subscriber addSubscriber(Subscriber subscriber) {
		subscriber.onTimeout(() -> this.subscribers.remove(subscriber));

		this.subscribers.add(subscriber);

		LOG.info("Total Subscribers {}" , subscribers.size());

		return subscriber;
	}

	@Async("notificationThreadPoolTaskExecutor")
	@EventListener
	@Override
	public void notifySubscribers(Notification notification) {
		List<Subscriber> deadSubscribers = new ArrayList<>();
		// loop through all subscribers
		for (Subscriber subscriber : this.subscribers) {
			try {
				LOG.info("Notification Data {}", notification);

				SseEventBuilder sseEventBuilder = SseEmitter.event()
						.id(LocalDateTime.now().toString())
						.data(notification, MediaType.APPLICATION_JSON)
						.reconnectTime(10_000L);
				// send the notification
				subscriber.send(sseEventBuilder.build());
				subscriber.onError(error -> {
					LOG.info("subscriber dropped out. Removing from list");
					subscriber.completeWithError(error);
					deadSubscribers.add(subscriber);
				});

			} catch (IOException e) {
				LOG.info("Failed To Notify ", e);
				// subscriber has disconnected and is no longer listening
				deadSubscribers.add(subscriber);
			}
		}

		LOG.info("notification done");

		if(!deadSubscribers.isEmpty()) {
			this.subscribers.removeAll(deadSubscribers);
			LOG.info("Removed Dead Subscribers");
		}
		LOG.info("Total Subscribers {}" , subscribers.size());
	}
}
