package hls_study.api.storage;

import java.io.InputStream;

public interface VideoStorageGateway {

	void upload(String key, InputStream content, long size);
	void delete(String key);

}