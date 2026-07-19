package hls_study.api.entity;

import hls_study.api.enums.VideoStatus;
import hls_study.api.exceptions.InvalidVideoStateTransitionException;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEntity {

	private static final Map<VideoStatus, Set<VideoStatus>> ALLOWED_TRANSITIONS = Map.of( //
			VideoStatus.UPLOADED, Set.of(VideoStatus.PROCESSING, VideoStatus.FAILED), //
			VideoStatus.PROCESSING, Set.of(VideoStatus.READY, VideoStatus.FAILED) //
	);

	@Id
	private String id;

	@Column(nullable = false, length = 255)
	private String originalFilename;

	@Column(nullable = false)
	private String baseKey;

	@Column(nullable = false)
	private String rawKey;

	@Column(nullable = false)
	private String contentType;

	private String path480p;
	private String path1080p;
	private String masterKey;

	@Builder.Default
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VideoStatus status = VideoStatus.UPLOADED;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	public void setStatus(VideoStatus newStatus) {
		transitionTo(newStatus);
	}

	public boolean canTransitionTo(VideoStatus newStatus) {
		return ALLOWED_TRANSITIONS.getOrDefault(this.status, Set.of()).contains(newStatus);
	}

	public void transitionTo(VideoStatus newStatus) {
		if (!canTransitionTo(newStatus)) {
			throw new InvalidVideoStateTransitionException(
					String.format("Cannot transition from %s to %s", this.status, newStatus));
		}

		this.status = newStatus;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
		if (this.status == null) {
			transitionTo(VideoStatus.UPLOADED);
		}
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

}