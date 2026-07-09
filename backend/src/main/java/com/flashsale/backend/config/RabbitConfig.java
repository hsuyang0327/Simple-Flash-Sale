package com.flashsale.backend.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
    /**
     * @description Declare direct exchange for flash-sale order routing
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    /**
     * @description Declare direct exchange for dead-letter routing
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    // === Queues ===
    @Bean
    /**
     * @description Declare queue for flash-sale order creation messages
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_ROUTING_KEY) // Failed order messages go here
                .build();
    }

    @Bean
    /**
     * @description Declare queue for order cancellation messages
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Queue cancelQueue() {
        return QueueBuilder.durable(CANCEL_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CANCEL_ROUTING_KEY) // Failed cancel messages go here
                .build();
    }

    @Bean
    /**
     * @description Declare TTL queue for order timeout auto-cancellation
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Queue ttlQueue() {
        return QueueBuilder.durable(TTL_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", CANCEL_ROUTING_KEY)
                .withArgument("x-message-ttl", 600000) // 10 minutes
                .build();
    }

    // === Dead Letter Queues ===
    @Bean
    /**
     * @description Declare dead-letter queue for failed order messages
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Queue orderDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_DLQ)
                .withArgument("x-max-length", 10000)
                .withArgument("x-overflow", "drop-head")
                .build();
    }

    @Bean
    /**
     * @description Declare dead-letter queue for failed cancel messages
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Queue cancelDeadLetterQueue() {
        return QueueBuilder.durable(CANCEL_DLQ)
                .withArgument("x-max-length", 10000)
                .withArgument("x-overflow", "drop-head")
                .build();
    }

    // === Bindings ===
    @Bean
    /**
     * @description Bind order queue to order exchange with routing key
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Binding bindingOrderQueue() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean
    /**
     * @description Bind cancel queue to order exchange with routing key
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Binding bindingCancelQueue() {
        return BindingBuilder.bind(cancelQueue()).to(orderExchange()).with(CANCEL_ROUTING_KEY);
    }

    @Bean
    /**
     * @description Bind TTL queue to order exchange with routing key
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Binding bindingTtlQueue() {
        return BindingBuilder.bind(ttlQueue()).to(orderExchange()).with(TTL_ROUTING_KEY);
    }

    // === DLQ Bindings ===
    @Bean
    /**
     * @description Bind order DLQ to dead-letter exchange
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Binding bindingOrderDlq() {
        return BindingBuilder.bind(orderDeadLetterQueue()).to(deadLetterExchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean
    /**
     * @description Bind cancel DLQ to dead-letter exchange
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public Binding bindingCancelDlq() {
        return BindingBuilder.bind(cancelDeadLetterQueue()).to(deadLetterExchange()).with(CANCEL_ROUTING_KEY);
    }

    // === General Config ===
    @Bean
    /**
     * @description Configure Jackson JSON message converter for RabbitMQ
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    /**
     * @description Configure RabbitTemplate with JSON converter and publisher confirms
     * @author Yang-Hsu
     * @date 2026/7/9
     */
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        template.setMandatory(true);
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (!ack) {
                log.error("[MQ] Message not confirmed by broker, cause: {}", cause);
            }
        });
        template.setReturnsCallback(returned ->
            log.error("[MQ] Message unroutable, routingKey: {}", returned.getRoutingKey()));
        return template;
    }

    /**
     * 專用於 ORDER_QUEUE 的 ContainerFactory：不進行 retry。
     * 原因：訂單建立失敗（如 MySQL 庫存不足）屬於不可重試錯誤；
     * 若使用 retry，catch block 中的 Redis 還原會被執行多次，造成庫存虛增。
     */
    @Bean
    public SimpleRabbitListenerContainerFactory noRetryContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
