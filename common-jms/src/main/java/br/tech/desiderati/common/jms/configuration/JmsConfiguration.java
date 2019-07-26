/*
 * Copyright (c) 2019 - Felipe Desiderati ALL RIGHTS RESERVED.
 *
 * This software is protected by international copyright laws and cannot be
 * used, copied, stored or distributed without prior authorization.
 */
package br.tech.desiderati.common.jms.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.policy.DeadLetterStrategy;
import org.apache.activemq.broker.region.policy.IndividualDeadLetterStrategy;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jms.DefaultJmsListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.util.ErrorHandler;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import java.net.URI;

@Slf4j
@EnableJms
@EnableAutoConfiguration
@SpringBootConfiguration
@PropertySource("classpath:jms.properties")
@ComponentScan("br.tech.desiderati.common.jms")
public class JmsConfiguration {

    public static final String TYPE_ID_PROPERTY_NAME = "_type";

    @Bean
    public Queue queue(@Value("${jms.default-queue.name}") String queueName) {
        return new ActiveMQQueue(queueName);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jms.dlq", name = "enabled", havingValue = "true")
    public Queue dlqQueue(@Value("${jms.dlq.queue-prefix}") String queueDlqPrefix,
                          @Value("${jms.default-queue.name}") String queueName) {
        return new ActiveMQQueue(queueDlqPrefix + queueName);
    }

    @Bean
    public Queue responseQueue(@Value("${jms.default-response-queue.name}") String responseQueueName) {
        return new ActiveMQQueue(responseQueueName);
    }

    @Bean
    @ConditionalOnProperty(prefix = "jms.dlq", name = "enabled", havingValue = "true")
    public Queue responseDlqQueue(@Value("${jms.dlq.queue-prefix}") String queueDlqPrefix,
                                  @Value("${jms.default-response-queue.name}") String responseQueueName) {
        return new ActiveMQQueue(queueDlqPrefix + responseQueueName);
    }

    /**
     * Registramos um gerenciador de filas embutido.
     */
    @Bean
    @ConditionalOnProperty(prefix = "jms.broker", name = "enabled", havingValue = "true")
    public BrokerService brokerService(@Value("${spring.activemq.broker-url}") String brokerUrl,
                                       @Value("${jms.broker.data-directory}") String dataDirectory,
                                       DeadLetterStrategy deadLetterStrategy) throws Exception {

        TransportConnector connector = new TransportConnector();
        connector.setUri(new URI(brokerUrl));

        BrokerService broker = new BrokerService();
        broker.addConnector(connector);
        broker.setUseShutdownHook(false);
        broker.setSystemExitOnShutdown(true);
        broker.setDataDirectory(dataDirectory);

        if (deadLetterStrategy != null) {
            PolicyEntry policy = new PolicyEntry();
            policy.setDeadLetterStrategy(deadLetterStrategy);

            PolicyMap policyMap = new PolicyMap();
            policyMap.setDefaultEntry(policy);
            broker.setDestinationPolicy(policyMap);
        }
        return broker;
    }

    /**
     * Registramos uma DLQ para a fila de entrada.
     */
    @Bean
    @ConditionalOnProperty(prefix = "jms.dlq", name = "enabled", havingValue = "true")
    public DeadLetterStrategy deadLetterStrategy(@Value("${jms.dlq.queue-prefix}") String queueDlqPrefix) {
        IndividualDeadLetterStrategy strategy = new IndividualDeadLetterStrategy();
        strategy.setProcessNonPersistent(true);
        strategy.setQueuePrefix(queueDlqPrefix);
        return strategy;
    }

    /**
     * Precisamos redefinir um {@link DefaultJmsListenerContainerFactory}, para que assim seja possível registrar
     * um {@link ErrorHandler} personalizado.
     * Aproveitamos e já registramos uma política de reentrega.
     */
    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            DefaultJmsListenerContainerFactoryConfigurer configurer,
            ConnectionFactory connectionFactory, @Qualifier("jmsErrorHandler") ErrorHandler errorHandler) {

        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler);

        // TODO Felipe Desiderati: Expor estas configurações via arquivo de propriedades quando for necessário modificá-las!
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setBackOffMultiplier(3);
        redeliveryPolicy.setInitialRedeliveryDelay(1000);
        redeliveryPolicy.setRedeliveryDelay(3000);
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setMaximumRedeliveries(5);
        if (connectionFactory instanceof ActiveMQConnectionFactory) {
            ((ActiveMQConnectionFactory) connectionFactory).setRedeliveryPolicy(redeliveryPolicy);
        } else {
            log.warn("It's not possible configure redelivery policy on " +
                "connection factory! Using default values instead.");
        }

        return factory;
    }

    /**
     * Registramos um conversor para JSON.
     */
    @Bean
    public MessageConverter jacksonJmsMessageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setTargetType(MessageType.TEXT);

        // TypeIdPropertyName is a name of a property that identifies the entity.
        // Jackson mapper should know what entity to use when deserializing incoming JSON.
        converter.setTypeIdPropertyName(TYPE_ID_PROPERTY_NAME);
        return converter;
    }
}
