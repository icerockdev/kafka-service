/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.sample

import com.icerockdev.service.kafka.IKafkaConsumer
import com.icerockdev.service.kafka.KafkaConsumerExecutionPool
import com.icerockdev.service.kafka.KafkaConsumerFactory
import com.icerockdev.service.kafka.KafkaSender
import kotlinx.coroutines.Dispatchers
import java.time.Duration
import java.util.*

object Main {
    @JvmStatic
    fun main(args: Array<String>) {

        val service = ProducerService()
        val consumer = TestKafkaConsumer()
        val executor = KafkaConsumerExecutionPool(Dispatchers.IO)
        consumer.run(executor, KAFKA_TOPIC)

        for (i in 1..20) {
            service.sendData("Sanded value: $i")
        }

        Thread.sleep(1000)

        println("Shutdown started")
        service.close()
        executor.close()
        consumer.close()
    }
}

const val KAFKA_SERVERS = "localhost:9092"
const val KAFKA_CLIENT_ID = "client.id"
const val KAFKA_TOPIC = "topic"
const val KAFKA_GROUP_ID = "group"


class ProducerService : AutoCloseable {
    private val authProducer = KafkaSender<String>(
        KAFKA_SERVERS,
        KAFKA_CLIENT_ID
    )

    fun sendData(model: String): Boolean {
        return authProducer.send(KAFKA_TOPIC, model)
    }


    override fun close() {
        authProducer.close()
    }
}

class TestKafkaConsumer(): IKafkaConsumer {

    private val consumerAuthClient =
        KafkaConsumerFactory.createConsumer<String>(
            KAFKA_SERVERS,
            resetOffset = KafkaConsumerFactory.Offset.LATEST,
            groupId = KAFKA_GROUP_ID
        )

    override fun run(executor: KafkaConsumerExecutionPool, topic: String) {
        executor.runExecutor(
            consumer = consumerAuthClient,
            topicList = Collections.singletonList(topic),
            pollWait = Duration.ofMillis(100)
        ) {
            this.forEach { record ->
                // apply readed values
                println("Read value: ${record.value()}")
            }
            true
        }

    }

    override fun close() {
        consumerAuthClient.close()
    }
}