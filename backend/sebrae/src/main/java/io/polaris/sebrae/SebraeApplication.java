package io.polaris.sebrae;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import io.polaris.sebrae.config.YouTubeProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(YouTubeProperties.class)
public class SebraeApplication {

	public static void main(String[] args) {
		SpringApplication.run(SebraeApplication.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper();
	}

}
