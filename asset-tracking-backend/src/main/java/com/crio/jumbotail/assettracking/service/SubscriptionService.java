package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.response.Notification;
import com.crio.jumbotail.assettracking.exchanges.response.Subscriber;

/**
 * To Add Subscriber for SSE and notify those subscribers
 */
public interface SubscriptionService {

	/**
	 * To add the subscriber
	 * @param subscriber the subscriber to add
	 * @return instance of the added subscriber
	 */
	Subscriber addSubscriber(Subscriber subscriber);

	/**
	 * To Notify all the subscribers
	 * @param data the notification to send
	 */
	void notifySubscribers(Notification data);

}
