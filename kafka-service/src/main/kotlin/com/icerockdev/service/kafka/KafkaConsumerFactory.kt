/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import com.fasterxml.jackson.core.type.TypeReference
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.*

object KafkaConsumerFactory {
    enum class Offset(val value: String) {
        LATEST("latest"),
        EARLIEST("earliest")
    }

    inline fun <reified V: Any> createConsumer(
        servers: String,
        groupId: String,
        maxPollRecords: Int = 50,
        sessionTimeoutMillis: Int = 30000,
        resetOffset: Offset = Offset.LATEST
    ): KafkaConsumer<String, V> {
        val props = Properties()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = servers
        props[ConsumerConfig.GROUP_ID_CONFIG] = groupId
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = maxPollRecords
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = sessionTimeoutMillis
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = resetOffset.value

        // Create the consumer using props.
        return KafkaConsumer(
            props,
            StringDeserializer(),
            ObjectDeserializer(object: TypeReference<V>() {})
        )
    }
}