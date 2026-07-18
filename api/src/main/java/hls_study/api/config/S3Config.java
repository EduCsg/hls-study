package hls_study.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

	@Value("${aws.s3.accessKeyId}")
	private String accessKeyId;

	@Value("${aws.s3.secretAccessKey}")
	private String secretAccessKey;

	@Value("${aws.s3.endpoint}")
	private String endpoint;

	@Bean
	public S3Client s3Client() {
		StaticCredentialsProvider credentials = StaticCredentialsProvider.create(
				AwsBasicCredentials.create(accessKeyId, secretAccessKey));

		return S3Client.builder() //
					   .endpointOverride(URI.create(endpoint)) //
					   .region(Region.SA_EAST_1) //
					   .credentialsProvider(credentials) //
					   .forcePathStyle(true) //
					   .build();
	}

}