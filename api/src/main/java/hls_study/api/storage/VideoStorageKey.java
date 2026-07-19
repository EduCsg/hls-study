package hls_study.api.storage;

import hls_study.api.enums.VideoQuality;

public record VideoStorageKey(String videoId) {

	public String base() {
		return "videos/" + videoId;
	}

	public String raw(String extension) {
		return base() + "/raw" + extension;
	}

	public String master() {
		return base() + "/master.m3u8";
	}

	public String playlist(VideoQuality quality) {
		return base() + "/" + quality.getValue() + "/playlist.m3u8";
	}

	public String segment(VideoQuality quality, int segmentNumber) {
		return base() + "/" + quality.getValue() + "/segment" + String.format("%03d", segmentNumber) + ".ts";
	}

}