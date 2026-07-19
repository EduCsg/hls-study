package hls_study.api.controller;

import hls_study.api.dto.VideoResponse;
import hls_study.api.dto.VideoUploadRequest;
import hls_study.api.entity.VideoEntity;
import hls_study.api.service.VideoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("videos")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@PostMapping("upload")
	public ResponseEntity<VideoResponse> upload(@Valid @ModelAttribute VideoUploadRequest request) {
		VideoEntity video = videoService.uploadVideo(request.file());
		return ResponseEntity.ok(VideoResponse.from(video, null));
	}

}