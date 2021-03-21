package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.Notification;
import com.crio.jumbotail.assettracking.exchanges.Subscriber;

public interface SubscriptionService {

	Subscriber subscribe(Subscriber subscriber);

	void notifySubscriber(Notification data);

}
