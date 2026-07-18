package hls_study.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record VideoUploadedEvent(@JsonProperty("video_id") String videoId, @JsonProperty("raw_key") String rawKey) {

}