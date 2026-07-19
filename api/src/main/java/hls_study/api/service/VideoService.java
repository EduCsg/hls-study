package hls_study.api.service;

import hls_study.api.dto.VideoUploadedEvent;
import hls_study.api.entity.VideoEntity;
import hls_study.api.event.VideoEventPublisher;
import hls_study.api.exceptions.GatewayException;
import hls_study.api.exceptions.VideoUploadException;
import hls_study.api.repository.VideoRepository;
import hls_study.api.storage.VideoStorageGateway;
import hls_study.api.storage.VideoStorageKey;
import hls_study.api.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

	private final VideoRepository videoRepository;
	private final VideoEventPublisher eventPublisher;
	private final VideoStorageGateway videoStorageGateway;

	public VideoEntity uploadVideo(MultipartFile videoFile) {
		VideoUtils.validateVideoFile(videoFile);

		String videoId = VideoUtils.generateVideoId();

		VideoStorageKey key = new VideoStorageKey(videoId);
		String baseVideoKey = key.base();
		String rawVideoKey = key.raw(VideoUtils.getFileExtension(videoFile.getOriginalFilename()));

		try {
			videoStorageGateway.upload(rawVideoKey, videoFile.getInputStream(), videoFile.getSize());
		} catch (GatewayException e) {
			throw new VideoUploadException("Erro ao fazer upload do vídeo para o S3: " + e.getMessage(), e);
		} catch (IOException e) {
			throw new VideoUploadException("Erro ao ler o arquivo de vídeo: " + e.getMessage(), e);
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
			eventPublisher.publishVideoUploaded(event);
		} catch (GatewayException e) {
			rollbackUpload(videoEntity, e);
			throw new VideoUploadException("Erro ao publicar evento de upload: " + e.getMessage(), e);
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
			videoStorageGateway.delete(rawVideoKey);
		} catch (Exception deleteException) {
			log.error("Falha ao remover objeto do S3 (key={}) durante rollback; objeto pode ter ficado órfão",
					rawVideoKey, deleteException);
		}
	}

}