/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class KafkaSender<T : Any>(
    servers: String,
    clientId: String
) :
    AutoCloseable {

    private val logger: Logger = LoggerFactory.getLogger(KafkaSender::class.java)
    private val producer =
        KafkaProducerFactory.createProducer<T>(
            servers = servers,
            clientId = clientId
        )

    fun send(topic: String, data: T): Boolean {
        val time = System.currentTimeMillis()

        try {
            val record: ProducerRecord<String, T> = ProducerRecord(
                topic,
                time.toString(),
                data
            )

            val metadata = producer.send(record).get()
            val elapsedTime = System.currentTimeMillis() - time
            logger.debug(
                String.format(
                    "sent record producer (key=%s value=%s) " + "meta(partition=%d, offset=%d) time=%d\n",
                    record.key(), record.value(), metadata.partition(),
                    metadata.offset(), elapsedTime
                )
            )
            return true
        } catch (exception: Exception) {
            logger.error(exception.localizedMessage, exception)
            return false
        } finally {
            producer.flush()
        }
    }

    fun sendAsync(topic: String, data: T) {
        val time = System.currentTimeMillis()
        val record: ProducerRecord<String, T> = ProducerRecord(topic, time.toString(), data)

        producer.send(record) { metadata, exception ->
            val elapsedTime = System.currentTimeMillis() - time
            if (metadata != null) {
                logger.debug(
                    String.format(
                        "sent record consumer (key=%s value=%s) " + "meta(partition=%d, offset=%d) time=%d\n",
                        record.key(), record.value(), metadata.partition(),
                        metadata.offset(), elapsedTime
                    )
                )
            } else {
                logger.error(exception.localizedMessage, exception)
            }
        }
    }

    override fun close() {
        producer.flush()
        producer.close()
    }
}
