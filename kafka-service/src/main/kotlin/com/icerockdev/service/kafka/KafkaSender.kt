/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.errors.InvalidPidMappingException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KafkaSender {
    private val logger: Logger = LoggerFactory.getLogger(KafkaSender::class.java)

    fun <K : Any, V : Any> send(producer: Producer<K, V>, topic: String, key: K, value: V): Boolean {
        val time = System.currentTimeMillis()

        try {
            val record: ProducerRecord<K, V> = ProducerRecord(
                topic,
                key,
                value
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
        } catch (exception: InvalidPidMappingException) {
            // retry after transaction expire (for transactions)
            return send(producer, topic, key, value)
        } catch (exception: Exception) {
            logger.error(exception.localizedMessage, exception)
            return false
        }
    }

    fun <K : Any, V : Any> sendAsync(producer: Producer<K, V>, topic: String, key: K, data: V) {
        val time = System.currentTimeMillis()
        val record: ProducerRecord<K, V> = ProducerRecord(topic, key, data)

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
                if (exception is InvalidPidMappingException) {
                    // retry after transaction expire (for transactions)
                    return@send sendAsync(producer, topic, key, data)
                }
                logger.error(exception.localizedMessage, exception)
            }
        }
    }
}
