package com.samialsohan.linkly.service;

import com.samialsohan.linkly.config.KafkaConfig;
import com.samialsohan.linkly.event.ClickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClickEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ClickEventPublisher.class);
    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public ClickEventPublisher(KafkaTemplate<String, ClickEvent> kafkaTemplate)
    {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publish(ClickEvent event)
    {
        try{
            kafkaTemplate.send(KafkaConfig.CLICKS_TOPIC, event.shortCode(), event)
                    .whenComplete((result, ex) ->{
                        if(ex != null)
                        {
                            log.warn("Failed to publish click for '{}': {}",
                                    event.shortCode(), ex.getMessage());
                        }
                    });
        }
        catch (Exception e)
        {
            log.warn("Click publish rejected for '{}': {}",
                    event.shortCode(), e.getMessage());
        }
    }
}
