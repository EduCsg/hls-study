package hls_study.api.config;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Component
@NullMarked
@RequiredArgsConstructor
public class S3BucketInitializer implements ApplicationRunner {

	private final S3Client s3Client;
	private final VideoProperties videoProperties;

	@Override
	public void run(ApplicationArguments args) {
		String bucketName = videoProperties.getBucketName();
		try {
			s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
		} catch (NoSuchBucketException e) {
			s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
		}
	}

}