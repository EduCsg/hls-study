package hls_study.api.adapter;

import hls_study.api.exceptions.GatewayException;
import hls_study.api.storage.VideoStorageGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.InputStream;

@Component
@RequiredArgsConstructor
public class S3VideoStorageGateway implements VideoStorageGateway {

	private final S3Client s3Client;

	@Value("${aws.s3.bucketName}")
	private String bucketName;

	@Override
	public void upload(String key, InputStream content, long size) {
		try {
			s3Client.putObject(b -> b.bucket(bucketName).key(key).build(), RequestBody.fromInputStream(content, size));
		} catch (Exception e) {
			throw new GatewayException("Erro ao fazer upload para o S3: " + e.getMessage(), e);
		}
	}

	@Override
	public void delete(String key) {
		try {
			s3Client.deleteObject(b -> b.bucket(bucketName).key(key).build());
		} catch (Exception e) {
			throw new GatewayException("Erro ao remover objeto do S3: " + e.getMessage(), e);
		}
	}

}