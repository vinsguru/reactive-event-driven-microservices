package com.vinsguru.cloudstreamkafkaplayground;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

import java.util.function.UnaryOperator;

@DirtiesContext
@SpringBootTest(properties = {
		"logging.level.root=ERROR",
		"logging.level.com.vinsguru*=INFO",
		"spring.cloud.stream.kafka.binder.configuration.auto.offset.reset=earliest"
})
@EmbeddedKafka(
		partitions = 1,
		bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
public abstract class AbstractIntegrationTest {

	@Autowired
	private EmbeddedKafkaBroker broker;

	protected <K, V> KafkaSender<K, V> createSender(UnaryOperator<SenderOptions<K, V>> builder){
		var props = KafkaTestUtils.producerProps(broker);
		var options = SenderOptions.<K, V>create(props);
		options = builder.apply(options);
		return KafkaSender.create(options);
	}

	protected <K,V> SenderRecord<K, V, K> toSenderRecord(String topic, K key, V value){
		return SenderRecord.create(topic, null, null, key, value, key);
	}

}
