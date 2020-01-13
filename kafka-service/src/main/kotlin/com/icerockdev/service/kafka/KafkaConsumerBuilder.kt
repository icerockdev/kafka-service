/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.Deserializer
import java.util.*

class KafkaConsumerBuilder {
    enum class Offset(val value: String) {
        LATEST("latest"),
        EARLIEST("earliest")
    }

    enum class IsolationLevel(val value: String) {
        READ_UNCOMMITTED("read_uncommitted"),
        READ_COMMITTED("read_committed")
    }

    val props: Properties = Properties()

    fun applyReadOpt(
        maxPollRecords: Int = 50,
        resetOffset: Offset = Offset.LATEST
    ): KafkaConsumerBuilder {
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = maxPollRecords
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = resetOffset.value
        return this
    }

    fun applyIsolation(level: IsolationLevel = IsolationLevel.READ_COMMITTED): KafkaConsumerBuilder {
        props[ConsumerConfig.ISOLATION_LEVEL_CONFIG] = level.value
        return this
    }

    fun <K : Any, V : Any> build(
        servers: String,
        groupId: String,
        clientId: String,
        keyDeserializer: Deserializer<K>,
        valueDeserializer: Deserializer<V>
    ): KafkaConsumer<K, V> {
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.CLIENT_ID_CONFIG] = clientId

        val consumer = KafkaConsumer(
            props,
            keyDeserializer,
            valueDeserializer
        )
        props.clear()

        return consumer
    }
}