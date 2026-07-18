package hls_study.api.controller;

import hls_study.api.dto.VideoResponse;
import hls_study.api.entity.VideoEntity;
import hls_study.api.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("videos")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@PostMapping("upload")
	public ResponseEntity<VideoResponse> upload(@RequestPart("file") MultipartFile file) {
		VideoEntity video = videoService.uploadVideo(file);
		return ResponseEntity.ok(VideoResponse.from(video, null));
	}

}