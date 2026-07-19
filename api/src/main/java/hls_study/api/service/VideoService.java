package hls_study.api.service;

import hls_study.api.dto.VideoUploadedEvent;
import hls_study.api.entity.VideoEntity;
import hls_study.api.exceptions.VideoUploadException;
import hls_study.api.repository.VideoRepository;
import hls_study.api.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

	private static final long KAFKA_SEND_TIMEOUT_SECONDS = 10;

	private final S3Client s3Client;
	private final VideoRepository videoRepository;
	private final KafkaTemplate<String, VideoUploadedEvent> kafkaTemplate;

	@Value("${aws.s3.bucketName}")
	private String bucketName;

	@Value("${kafka.topic.videoUploaded}")
	private String videoUploadedTopic;

	public VideoEntity uploadVideo(MultipartFile videoFile) {
		VideoUtils.validateVideoFile(videoFile);

		String videoId = VideoUtils.generateVideoId();
		String baseVideoKey = VideoUtils.getBaseVideoKey(videoId);
		String rawVideoKey = VideoUtils.getRawVideoKey(baseVideoKey, videoFile);

		try {
			s3Client.putObject(builder -> builder.bucket(bucketName).key(rawVideoKey).build(),
					RequestBody.fromInputStream(videoFile.getInputStream(), videoFile.getSize()));
		} catch (Exception e) {
			throw new VideoUploadException("Erro ao fazer upload do vídeo para o S3: " + e.getMessage(), e);
		}

		log.info("Video uploaded to S3 with key: {}", rawVideoKey);

		VideoEntity videoEntity = VideoEntity.builder() //
											 .id(videoId) //
											 .baseKey(baseVideoKey) //
											 .rawKey(rawVideoKey) //
											 .contentType(videoFile.getContentType()) //
											 .originalFilename(videoFile.getOriginalFilename()) //
											 .build();

		try {
			videoEntity = videoRepository.save(videoEntity);
		} catch (Exception e) {
			compensateS3Upload(rawVideoKey);
			log.warn("Upload compensado no S3 (key={}) após falha ao salvar metadados", rawVideoKey, e);
			throw new VideoUploadException("Erro ao salvar metadados do vídeo: " + e.getMessage(), e);
		}

		log.info("Video metadata saved to database for videoId: {}", videoId);

		publishUploadedEvent(videoEntity);

		return videoEntity;
	}

	private void publishUploadedEvent(VideoEntity videoEntity) {
		VideoUploadedEvent event = VideoUploadedEvent.builder() //
													 .videoId(videoEntity.getId()) //
													 .rawKey(videoEntity.getRawKey()) //
													 .baseKey(videoEntity.getBaseKey()) //
													 .contentType(videoEntity.getContentType()) //
													 .originalFilename(videoEntity.getOriginalFilename()) //
													 .build();

		try {
			kafkaTemplate.send(videoUploadedTopic, videoEntity.getId(), event)
						 .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			rollbackUpload(videoEntity, e);
			throw new VideoUploadException("Publicação do evento no Kafka interrompida: " + e.getMessage(), e);
		} catch (Exception e) {
			rollbackUpload(videoEntity, e);
			throw new VideoUploadException("Erro ao publicar evento de upload no Kafka: " + e.getMessage(), e);
		}
	}

	private void rollbackUpload(VideoEntity videoEntity, Throwable originalCause) {
		compensateS3Upload(videoEntity.getRawKey());

		try {
			videoRepository.deleteById(videoEntity.getId());
		} catch (Exception deleteException) {
			log.error(
					"Falha ao remover registro do banco (videoId={}) durante rollback. Registro pode ter ficado inconsistente",
					videoEntity.getId(), deleteException);
			return;
		}

		log.warn("Upload revertido (videoId={}) após falha ao publicar evento no Kafka", videoEntity.getId(),
				originalCause);
	}

	private void compensateS3Upload(String rawVideoKey) {
		try {
			s3Client.deleteObject(builder -> builder.bucket(bucketName).key(rawVideoKey).build());
		} catch (Exception deleteException) {
			log.error("Falha ao remover objeto do S3 (key={}) durante rollback; objeto pode ter ficado órfão",
					rawVideoKey, deleteException);
		}
	}

}