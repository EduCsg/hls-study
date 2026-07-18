package hls_study.api.repository;

import hls_study.api.entity.VideoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoRepository extends JpaRepository<VideoEntity, String> {

}