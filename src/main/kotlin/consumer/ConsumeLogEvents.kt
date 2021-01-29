package consumer

import models.ThomsonLogLineModel
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.*
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KTable
import org.apache.kafka.streams.kstream.Produced
//import org.apache.logging.log4j.LogManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import serde.ThomsonLogLineSerde
import java.util.*
import java.util.concurrent.CountDownLatch


class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            ConsumeLogEvents().run(args)
        }
    }
}

class ConsumeLogEvents {

    companion object {
//        slf4 logger
        private val logger: Logger = LoggerFactory.getLogger(ConsumeLogEvents::class.java)
//        private val logger = LogManager.getLogger(ConsumeLogEvents::class.java)
    }

    val INPUT_TOPIC  = "thomson_ultamate_logger"
    val OUTPUT_TOPIC  = "thomson_out"

    fun getStreamsConfig(): Properties {
        val props = Properties()
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streams-elisa")
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0)
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::javaClass.get())

        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String()::javaClass.get())
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String()::javaClass.get())

        props.put("default.deserialization.exception.handler", LogAndContinueExceptionHandler::class.java)
        props.put(
            StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
            LogAndContinueExceptionHandler::class.java
        );

        return props
    }

    fun consumeAlerts(builder: StreamsBuilder) {

        val props = getStreamsConfig()

        logger.debug("FITTAN!!")

        val stringSerde = Serdes.String()
        val longSerde = Serdes.Long()

        val thomsonLogLineSerde: Serde<ThomsonLogLineModel> =
            ThomsonLogLineSerde()

        val produced: Produced<String, String> =
            Produced.with(stringSerde, stringSerde)

        val consumed: Consumed<String, ThomsonLogLineModel> =
            Consumed.with(stringSerde, thomsonLogLineSerde)

        val source = builder.stream(INPUT_TOPIC, consumed)

        source
            .mapValues(ThomsonLogLineModel::toString)
            .peek({k ,v -> logger.debug("tuleeko mitään {}{}" , k, v)})
            .to("thomson_test", produced)


    }

    fun run(args: Array<String>) {
        val props = getStreamsConfig()
        val builder: StreamsBuilder = StreamsBuilder()
        consumeAlerts(builder)

        val topo: Topology = builder.build()

        val streams: KafkaStreams = KafkaStreams(topo, props)
        val latch: CountDownLatch = CountDownLatch(1)

        Runtime.getRuntime().addShutdownHook(object : Thread("streams-wordcount-shutdown-hook") {
            override fun run() {
                streams.close()
                streams.cleanUp()
                latch.countDown()
            }
        })

        try {
            streams.cleanUp()
            streams.start()
            latch.await()
        } catch (e: Throwable) {
            System.exit(1)
        }

        System.exit(0)

    }
}