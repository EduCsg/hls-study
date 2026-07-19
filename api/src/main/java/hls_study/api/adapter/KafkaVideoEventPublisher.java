package hls_study.api.adapter;

import hls_study.api.dto.VideoUploadedEvent;
import hls_study.api.event.VideoEventPublisher;
import hls_study.api.exceptions.GatewayException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class KafkaVideoEventPublisher implements VideoEventPublisher {

	private static final long KAFKA_SEND_TIMEOUT_SECONDS = 10;

	private final KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;

	@Value("${kafka.topic.videoUploaded}")
	private String videoUploadedTopic;

	@Override
	public void publishVideoUploaded(VideoUploadedEvent event) {
		try {
			kafkaTemplate.send(videoUploadedTopic, event.videoId(), event)
						 .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new GatewayException("Publicação do evento no Kafka interrompida: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new GatewayException("Erro ao publicar evento no Kafka: " + e.getMessage(), e);
		}
	}

}