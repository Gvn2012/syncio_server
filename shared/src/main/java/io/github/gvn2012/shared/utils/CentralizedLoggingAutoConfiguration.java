package io.github.gvn2012.shared.utils;

import io.github.gvn2012.shared.kafka_events.CentralizedLogEvent;
import io.micrometer.tracing.Tracer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.server.WebFilter;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@EnableConfigurationProperties(CentralizedLoggingProperties.class)
@ConditionalOnProperty(prefix = "syncio.centralized-logging", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CentralizedLoggingAutoConfiguration {

    @Bean(name = "centralizedLoggingTaskExecutor")
    @ConditionalOnMissingBean(name = "centralizedLoggingTaskExecutor")
    public TaskExecutor centralizedLoggingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("centralized-log-");
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(1000);
        executor.initialize();
        return executor;
    }

    @Bean(name = "centralizedLogProducerFactory")
    @ConditionalOnMissingBean(name = "centralizedLogProducerFactory")
    public ProducerFactory<String, CentralizedLogEvent> centralizedLogProducerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> config = new HashMap<>(kafkaProperties.buildProducerProperties(null));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean(name = "centralizedLogKafkaTemplate")
    @ConditionalOnMissingBean(name = "centralizedLogKafkaTemplate")
    public KafkaTemplate<String, CentralizedLogEvent> centralizedLogKafkaTemplate(
            @Qualifier("centralizedLogProducerFactory") ProducerFactory<String, CentralizedLogEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public CentralizedLogProducer centralizedLogProducer(
            @Qualifier("centralizedLogKafkaTemplate") KafkaTemplate<String, CentralizedLogEvent> kafkaTemplate,
            @Qualifier("centralizedLoggingTaskExecutor") TaskExecutor taskExecutor,
            CentralizedLoggingProperties properties,
            Tracer tracer,
            @Value("${spring.application.name:unknown-service}") String serviceName) {
        return new CentralizedLogProducer(kafkaTemplate, taskExecutor, properties, tracer, serviceName);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnClass(OncePerRequestFilter.class)
    public ServletCentralizedLoggingFilter servletCentralizedLoggingFilter(
            CentralizedLoggingProperties properties,
            CentralizedLogProducer centralizedLogProducer,
            Tracer tracer,
            @Value("${spring.application.name:unknown-service}") String serviceName) {
        return new ServletCentralizedLoggingFilter(properties, centralizedLogProducer, tracer, serviceName);
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnClass(WebFilter.class)
    public ReactiveCentralizedLoggingFilter reactiveCentralizedLoggingFilter(
            CentralizedLoggingProperties properties,
            CentralizedLogProducer centralizedLogProducer,
            Tracer tracer) {
        return new ReactiveCentralizedLoggingFilter(properties, centralizedLogProducer, tracer);
    }
}
