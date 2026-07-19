package hls_study.api.event;

import hls_study.api.dto.VideoUploadedEvent;

public interface VideoEventPublisher {

	void publishVideoUploaded(VideoUploadedEvent event);

}