package hls_study.api.config;

import hls_study.api.dto.VideoUploadedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaProducerConfig {

	@Bean
	public KafkaTemplate<String, VideoUploadedEvent> videoUploadedEventKafkaTemplate(
			ProducerFactory<String, VideoUploadedEvent> producerFactory) {
		return new KafkaTemplate<>(producerFactory);
	}

}