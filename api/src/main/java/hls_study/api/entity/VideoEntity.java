package hls_study.api.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "videos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VideoEntity {

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

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private VideoStatus status;

	@Column(nullable = false, updatable = false)
	private Instant createdAt;

	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		this.createdAt = Instant.now();
		this.updatedAt = this.createdAt;
		if (this.status == null) {
			this.status = VideoStatus.UPLOADED;
		}
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}

}