package com.launchpad.flow.engine.executors;

import com.launchpad.flow.engine.NodeExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TriggerExecutorsConfig {
    @Bean
    NodeExecutor webhookTriggerExecutor() {
        return new TriggerNodeExecutor.Webhook();
    }

    @Bean
    NodeExecutor scheduleTriggerExecutor() {
        return new TriggerNodeExecutor.Schedule();
    }

    @Bean
    NodeExecutor manualTriggerExecutor() {
        return new TriggerNodeExecutor.Manual();
    }
}

