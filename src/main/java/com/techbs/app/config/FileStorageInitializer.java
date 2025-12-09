package com.techbs.app.config;

import com.techbs.app.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileStorageInitializer implements ApplicationRunner {

    private final FileStorageService fileStorageService;

    @Override
    public void run(ApplicationArguments args) {
        fileStorageService.init();
    }
}