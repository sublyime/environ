package com.fairchild.envmonitor.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Value("${external-apis.weather-gov.base-url}")
    private String weatherGovBaseUrl;

    @Value("${external-apis.weather-gov.user-agent}")
    private String userAgent;

    @Value("${external-apis.open-meteo.base-url}")
    private String openMeteoBaseUrl;

    @Value("${external-apis.marine-data.base-url}")
    private String marineDataBaseUrl;

    @Value("${external-apis.air-quality.base-url}")
    private String airQualityBaseUrl;

    @Bean("weatherGovWebClient")
    public WebClient weatherGovWebClient() {
        return WebClient.builder()
                .baseUrl(weatherGovBaseUrl)
                .defaultHeader("User-Agent", userAgent)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Bean("openMeteoWebClient")
    public WebClient openMeteoWebClient() {
        return WebClient.builder()
                .baseUrl(openMeteoBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Bean("marineDataWebClient")
    public WebClient marineDataWebClient() {
        return WebClient.builder()
                .baseUrl(marineDataBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Bean("airQualityWebClient")
    public WebClient airQualityWebClient() {
        return WebClient.builder()
                .baseUrl(airQualityBaseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Bean("genericWebClient")
    public WebClient genericWebClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}
