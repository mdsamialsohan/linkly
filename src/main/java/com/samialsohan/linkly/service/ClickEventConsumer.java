package com.samialsohan.linkly.service;

import com.samialsohan.linkly.config.KafkaConfig;
import com.samialsohan.linkly.entity.Click;
import com.samialsohan.linkly.event.ClickEvent;
import com.samialsohan.linkly.repository.ClickRepository;
import com.samialsohan.linkly.repository.UrlRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ClickEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(ClickEventConsumer.class);
    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    public ClickEventConsumer(ClickRepository clickRepository, UrlRepository urlRepository)
    {
        this.clickRepository = clickRepository;
        this.urlRepository = urlRepository;
    }

    @KafkaListener(
            topics = KafkaConfig.CLICKS_TOPIC,
            autoStartup = "${linkly.consumer.enabled:true}"
    )
    @Transactional
    public void onClickEvent(ClickEvent event)
    {
        Click click = Click.builder()
                .shortCode(event.shortCode())
                .clickedAt(event.clickedAt())
                .referrer(truncate(event.referrer(), 2048))
                .userAgent(truncate(event.userAgent(), 512))
                .build();
        clickRepository.save(click);

        int updated = urlRepository.incrementClickCount(event.shortCode());
        if(updated == 0)
        {
            log.warn("Click received for unknown short code '{}'", event.shortCode());
        }
    }

    private static String truncate(String value, int max)
    {
        if(value == null || value.length() <= max)
        {
            return value;
        }
        return value.substring(0,max);
    }

}
