package hls_study.api.adapter;

import hls_study.api.config.VideoProperties;
import hls_study.api.dto.VideoUploadedEvent;
import hls_study.api.event.VideoEventPublisher;
import hls_study.api.exceptions.GatewayException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@NotNull
@RequiredArgsConstructor
public class KafkaVideoEventPublisher implements VideoEventPublisher {

	private final KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;
	private final VideoProperties videoProperties;

	@Override
	public void publishVideoUploaded(VideoUploadedEvent event) {
		try {
			kafkaTemplate.send(videoProperties.getUploadedTopic(), event.videoId(), event)
						 .get(videoProperties.getKafkaSendTimeoutSeconds(), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new GatewayException("Publicação do evento no Kafka interrompida: " + e.getMessage(), e);
		} catch (Exception e) {
			throw new GatewayException("Erro ao publicar evento no Kafka: " + e.getMessage(), e);
		}
	}

}