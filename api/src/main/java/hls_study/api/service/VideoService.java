package hls_study.api.service;

import hls_study.api.entity.VideoEntity;
import hls_study.api.exceptions.VideoUploadException;
import hls_study.api.repository.VideoRepository;
import hls_study.api.utils.VideoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@Service
@RequiredArgsConstructor
@Slf4j
public class VideoService {

	private final S3Client s3Client;
	private final VideoRepository videoRepository;

	@Value("${aws.s3.bucketName}")
	private String bucketName;

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
											 .originalFilename(videoFile.getOriginalFilename()) //
											 .build();

		try {
			videoEntity = videoRepository.save(videoEntity);
		} catch (Exception e) {
			compensateS3Upload(rawVideoKey, e);
			throw new VideoUploadException("Erro ao salvar metadados do vídeo: " + e.getMessage(), e);
		}

		log.info("Video metadata saved to database for videoId: {}", videoId);

		return videoEntity;
	}

	private void compensateS3Upload(String rawVideoKey, Exception originalCause) {
		try {
			s3Client.deleteObject(builder -> builder.bucket(bucketName).key(rawVideoKey).build());
			log.warn("Upload compensado no S3 (key={}) após falha ao salvar metadados", rawVideoKey, originalCause);
		} catch (Exception deleteException) {
			log.error("Falha ao compensar upload no S3 (key={}); objeto pode ter ficado órfão", rawVideoKey,
					deleteException);
		}
	}

}