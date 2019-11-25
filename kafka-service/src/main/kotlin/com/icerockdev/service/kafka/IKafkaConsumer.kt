/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

interface IKafkaConsumer: AutoCloseable {
    fun run(executor: KafkaConsumerExecutionPool, topic: String)
}