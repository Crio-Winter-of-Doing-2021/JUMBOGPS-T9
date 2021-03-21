//package com.crio.jumbotail.assettracking.notifications;
//
//import lombok.extern.log4j.Log4j2;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//@Log4j2
//@Component
//public class Runner implements CommandLineRunner {
//
//  private final RabbitTemplate rabbitTemplate;
////  private final Receiver receiver;
//
//  public Runner(/*Receiver receiver,*/ RabbitTemplate rabbitTemplate) {
////    this.receiver = receiver;
//    this.rabbitTemplate = rabbitTemplate;
//  }
//
//  @Override
//  public void run(String... args) throws Exception {
//    LOG.info("Sending message...");
//    rabbitTemplate.convertAndSend(MessageProducer.TOPIC_EXCHANGE_NAME, "geofence.notification.1", "Hello from RabbitMQ!");
////    receiver.getLatch().await(10000, TimeUnit.MILLISECONDS);
//  }
//
//}