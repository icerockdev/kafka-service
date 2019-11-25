/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.clients.producer.Producer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*
import kotlin.reflect.KClass

object KafkaProducerFactory {
    fun <V : Any> createProducer(
        servers: String,
        clientId: String,
        partitionerClass: KClass<out Partitioner> = CustomPartitioner::class
    ): Producer<String, V> {
        val props = Properties()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        props[ProducerConfig.CLIENT_ID_CONFIG] = clientId
        props[ProducerConfig.BUFFER_MEMORY_CONFIG] = 33554432
        props[ProducerConfig.BATCH_SIZE_CONFIG] = 16384
        props[ProducerConfig.LINGER_MS_CONFIG] = 5
        props[ProducerConfig.RETRIES_CONFIG] = 0
        props[ProducerConfig.ACKS_CONFIG] = "all"
        props[ProducerConfig.PARTITIONER_CLASS_CONFIG] = partitionerClass.java.name
        props[ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG] = 500
        props[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 1000

        return KafkaProducer(props,
            StringSerializer(),
            ObjectSerializer<V>()
        )
    }
}