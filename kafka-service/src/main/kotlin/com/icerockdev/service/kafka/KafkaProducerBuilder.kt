/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.internals.DefaultPartitioner
import org.apache.kafka.common.serialization.Serializer
import java.util.*
import kotlin.reflect.KClass

class KafkaProducerBuilder {
    val props: Properties = Properties()

    fun applyIdempotence(): KafkaProducerBuilder {
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.RETRIES_CONFIG] = Integer.MAX_VALUE
        props[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
        props[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 1
        return this
    }

    fun applyTransactional(transactionalId: String): KafkaProducerBuilder {
        applyIdempotence()
        props[ProducerConfig.TRANSACTIONAL_ID_CONFIG] = transactionalId
        return this
    }

    fun applyTimeout(lingerMs: Int = 5, requestTimeoutMs: Int = 500, deliveryTimeoutMs: Int = 1000): KafkaProducerBuilder {
        props[ProducerConfig.LINGER_MS_CONFIG] = lingerMs
        props[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = requestTimeoutMs
        props[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = deliveryTimeoutMs
        return this
    }

    fun applyBuffering(bufferMemory: Long = 32 * 1024 * 1024L, batchSize: Int = 16384): KafkaProducerBuilder {
        props[ProducerConfig.BUFFER_MEMORY_CONFIG] = bufferMemory
        props[ProducerConfig.BATCH_SIZE_CONFIG] = batchSize
        return this
    }

    fun <K : Any, V : Any> build(
        servers: String,
        clientId: String,
        partitionerClass: KClass<out Partitioner> = DefaultPartitioner::class,
        keySerializer: Serializer<K>,
        valueSerializer: Serializer<V>
    ): KafkaProducer<K, V> {
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        props[ProducerConfig.CLIENT_ID_CONFIG] = clientId
        props[ProducerConfig.PARTITIONER_CLASS_CONFIG] = partitionerClass.java.name

        val consumer = KafkaProducer(
            props,
            keySerializer,
            valueSerializer
        )
        props.clear()

        return consumer
    }
}