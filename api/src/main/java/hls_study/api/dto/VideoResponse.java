package hls_study.api.dto;

import hls_study.api.entity.VideoEntity;

public record VideoResponse(String id, String status, String masterUrl) {

	public static VideoResponse from(VideoEntity video, String masterUrl) {
		return new VideoResponse(video.getId(), video.getStatus().name(), masterUrl);
	}

}