/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import com.icerockdev.service.kafka.*
import kotlinx.coroutines.Dispatchers
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.LongSerializer
import org.apache.kafka.common.serialization.StringDeserializer
import java.net.InetAddress
import java.time.Duration
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        val hostname: String = InetAddress.getLocalHost().hostName
        // specify unique client id by hostname and app name
        val clientId = "app-name::${hostname}"

        val servers = "localhost:9092"
        val groupId = "group"
        val topic = "topic"

        println(clientId)

        val service = TestProducerService(servers, clientId, topic)
        val consumer = TestKafkaConsumer(servers, groupId, clientId)
        val executor = KafkaConsumerExecutionPool(Dispatchers.IO)
        consumer.run(executor, topic)

        for (i in 1..9) {
            service.sendAsyncData("Sanded value: $i")
        }
        for (i in 10..20) {
            service.sendData("Sanded value: $i")
        }

        Thread.sleep(1000)

        println("Shutdown started")
        service.close()
        executor.close()
        consumer.close()
    }
}

class TestProducerService(servers: String, clientId: String, private val topic: String) : AutoCloseable {
    private val producer = KafkaProducerBuilder()
//        .applyTransactional(KAFKA_TRANSACTION_ID) // supported only for 3 brokers and more
        .applyIdempotence()
        .applyTimeout()
        .applyBuffering()
        .build<Long, String>(
            servers = servers,
            clientId = clientId,
            keySerializer = LongSerializer(),
            valueSerializer = ObjectSerializer()
        )

    fun sendData(model: String): Boolean {
        val time = System.currentTimeMillis()
        return KafkaSender.send(producer, topic, time, model)
    }

    fun sendAsyncData(model: String) {
        val time = System.currentTimeMillis()
        KafkaSender.sendAsync(producer, topic, time, model)
    }

    override fun close() {
        producer.flush()
        producer.close()
    }
}

class TestKafkaConsumer(servers: String, groupId: String, clientId: String) : IKafkaConsumer {

    private val consumer =
        KafkaConsumerBuilder()
            .applyReadOpt()
            .applyIsolation(KafkaConsumerBuilder.IsolationLevel.READ_COMMITTED)
            .apply {
                with(props) {
                    this[ConsumerConfig.FETCH_MAX_BYTES_CONFIG] = 50 * 1024 * 1024
                    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = KafkaConsumerBuilder.Offset.LATEST.value
                }
            }
            .build<String, String>(
                servers,
                groupId,
                clientId,
                StringDeserializer(),
                ObjectDeserializer(jacksonTypeRef())
            )


    override fun run(executor: KafkaConsumerExecutionPool, topic: String) {
        executor.runExecutor(
            consumer = consumer,
            topicList = Collections.singletonList(topic),
            pollWait = Duration.ofMillis(100)
        ) {
            this.forEach { record ->
                // apply values
                println("Read value: ${record.value()}")
            }
            true
        }

    }

    override fun close() {
        consumer.close()
    }
}