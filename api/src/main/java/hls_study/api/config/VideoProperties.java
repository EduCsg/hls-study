package hls_study.api.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("app.video")
public class VideoProperties {

	@NotBlank
	private String bucketName;

	@NotBlank
	private String uploadedTopic;

	@NotBlank
	private String uploaded480pTopic;

	@NotBlank
	private String uploaded1080pTopic;

	@NotBlank
	private String generateMasterTopic;

	@NotBlank
	private String updateStatusTopic;

	@NotNull
	@Positive
	private Long kafkaSendTimeoutSeconds;

}
