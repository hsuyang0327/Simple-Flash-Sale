package com.flashsale.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;

@Configuration
public class RabbitConfig {

    // Exchanges
    public static final String ORDER_EXCHANGE = "flashsale.order.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "flashsale.deadletter.exchange";

    // Queues
    public static final String ORDER_QUEUE = "flashsale.order.queue";
    public static final String CANCEL_QUEUE = "flashsale.cancel.queue";
    public static final String TTL_QUEUE = "flashsale.ttl.queue";
    public static final String ORDER_DLQ = "flashsale.order.dlq";
    public static final String CANCEL_DLQ = "flashsale.cancel.dlq";

    // Routing Keys
    public static final String ORDER_ROUTING_KEY = "flashsale.order.key";
    public static final String CANCEL_ROUTING_KEY = "flashsale.cancel.key";
    public static final String TTL_ROUTING_KEY = "flashsale.ttl.key";

    // === Exchanges ===
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // === Queues ===
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_ROUTING_KEY) // Failed order messages go here
                .build();
    }

    @Bean
    public Queue cancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CANCEL_ROUTING_KEY) // Failed cancel messages go here
                .build();
    }

    @Bean
    public Queue ttlQueue() {
        return QueueBuilder.durable(TTL_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CANCEL_ROUTING_KEY)
                .withArgument("x-message-ttl", 600000) // 10 minutes
                .build();
    }

    // === Dead Letter Queues ===
    @Bean
    public Queue orderDeadLetterQueue() {
        return new Queue(ORDER_DLQ, true);
    }

    @Bean
    public Queue cancelDeadLetterQueue() {
        return new Queue(CANCEL_DLQ, true);
    }

    // === Bindings ===
    @Bean
    public Binding bindingOrderQueue() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding bindingCancelQueue() {
        return BindingBuilder.bind(cancelQueue()).to(orderExchange()).with(CANCEL_ROUTING_KEY);
    }

    @Bean
    public Binding bindingTtlQueue() {
        return BindingBuilder.bind(ttlQueue()).to(orderExchange()).with(TTL_ROUTING_KEY);
    }

    // === DLQ Bindings ===
    @Bean
    public Binding bindingOrderDlq() {
        return BindingBuilder.bind(orderDeadLetterQueue()).to(deadLetterExchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding bindingCancelDlq() {
        return BindingBuilder.bind(cancelDeadLetterQueue()).to(deadLetterExchange()).with(CANCEL_ROUTING_KEY);
    }

    // === General Config ===
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
