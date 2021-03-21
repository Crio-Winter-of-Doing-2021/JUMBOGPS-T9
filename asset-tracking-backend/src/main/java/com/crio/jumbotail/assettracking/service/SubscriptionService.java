package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.response.Notification;
import com.crio.jumbotail.assettracking.exchanges.response.Subscriber;

public interface SubscriptionService {

	Subscriber subscribe(Subscriber subscriber);

	void notifySubscriber(Notification data);

}
