//package com.crio.jumbotail.assettracking.notifications;
//
//import lombok.extern.log4j.Log4j2;
//import org.springframework.amqp.core.TopicExchange;
//import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Log4j2
////@Service
//@Configuration
//public class MessageProducer {
//
//
//
//
////	@Scheduled(fixedDelay = 1000, initialDelay = 500)
////	public void send() {
////		String message = "Hello World!";
////		this.template.convertAndSend(queue().getName(), message);
////		System.out.println(" [x] Sent '" + message + "'");
////	}
//
//
////	@Bean
////	Queue queue() {
////		return new Queue(QUEUE_NAME, false);
////	}
//
//
//
////	@Bean
////	Binding binding(Queue queue, TopicExchange exchange) {
////		return BindingBuilder
////				.bind(queue)
////				.to(exchange)
////				.with("geofence.notification.#");
////	}
//
////	@Bean
////	SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
////	                                         MessageListenerAdapter listenerAdapter) {
////		SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
////		container.setConnectionFactory(connectionFactory);
////		container.setQueueNames(queueName);
////		container.setMessageListener(listenerAdapter);
////		return container;
////	}
//
////	@Bean
////	MessageListenerAdapter listenerAdapter(Receiver receiver) {
////		return new MessageListenerAdapter(receiver, "receiveMessage");
////	}
//
////	@Autowired
////	private JmsTemplate jmsTemplate;
////
////	@Override
////	public void run(String... strings) throws Exception {
////
////		LOG.info("Queue Initialized");
////
////		String msg = "Hello World";
////		this.jmsTemplate.convertAndSend("testQueue", msg);
////
////		LOG.info("Queue Message Sent");
////	}
//
//}