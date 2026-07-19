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

	@NotNull
	@Positive
	private Long kafkaSendTimeoutSeconds;

}
