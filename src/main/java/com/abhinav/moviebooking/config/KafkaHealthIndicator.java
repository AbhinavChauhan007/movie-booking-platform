package com.abhinav.moviebooking.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Component
public class KafkaHealthIndicator implements HealthIndicator {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Override
    public Health health() {
        try {
            Properties props = new Properties();
            props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 3000);

            try (AdminClient adminClient = AdminClient.create(props)) {
                adminClient.describeCluster().clusterId().get(3, TimeUnit.SECONDS);
                return Health.up().withDetail("kafka", "Available").build();
            }
        } catch (Exception e) {
            return Health.down().withDetail("kafka", e.getMessage()).build();
        }
    }
}
