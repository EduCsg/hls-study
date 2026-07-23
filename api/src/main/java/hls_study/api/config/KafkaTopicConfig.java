package hls_study.api.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

	private static final int PARTITIONS = 3;
	private static final short REPLICATION_FACTOR = 3;

	@Bean
	public NewTopic videosUploadedTopic(VideoProperties videoProperties) {
		return TopicBuilder.name(videoProperties.getUploadedTopic())
				.partitions(PARTITIONS)
				.replicas(REPLICATION_FACTOR)
				.build();
	}

	@Bean
	public NewTopic videos480pUploadedTopic(VideoProperties videoProperties) {
		return TopicBuilder.name(videoProperties.getUploaded480pTopic())
				.partitions(PARTITIONS)
				.replicas(REPLICATION_FACTOR)
				.build();
	}

	@Bean
	public NewTopic videos1080pUploadedTopic(VideoProperties videoProperties) {
		return TopicBuilder.name(videoProperties.getUploaded1080pTopic())
				.partitions(PARTITIONS)
				.replicas(REPLICATION_FACTOR)
				.build();
	}

	@Bean
	public NewTopic videosGenerateMasterTopic(VideoProperties videoProperties) {
		return TopicBuilder.name(videoProperties.getGenerateMasterTopic())
				.partitions(PARTITIONS)
				.replicas(REPLICATION_FACTOR)
				.build();
	}

	@Bean
	public NewTopic videosUpdateStatusTopic(VideoProperties videoProperties) {
		return TopicBuilder.name(videoProperties.getUpdateStatusTopic())
				.partitions(PARTITIONS)
				.replicas(REPLICATION_FACTOR)
				.build();
	}

}
