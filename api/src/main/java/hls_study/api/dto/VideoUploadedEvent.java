package hls_study.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record VideoUploadedEvent(@JsonProperty("video_id") String videoId, @JsonProperty("raw_key") String rawKey,
								 @JsonProperty("base_key") String baseKey,
								 @JsonProperty("content_type") String contentType,
								 @JsonProperty("original_filename") String originalFilename) {

}