package com.valueplus.app.config;

import com.valueplus.domain.service.concretes.StartUpService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class StartUpEvent implements ApplicationListener<ApplicationReadyEvent> {
    private final StartUpService startUpService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        startUpService.loadDefaultData();
    }
}
