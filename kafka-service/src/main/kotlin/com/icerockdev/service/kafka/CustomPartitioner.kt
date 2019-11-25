/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import org.apache.kafka.clients.producer.Partitioner
import org.apache.kafka.common.Cluster

class CustomPartitioner : Partitioner {

    override fun configure(configs: Map<String, *>) {}

    override fun partition(
        topic: String,
        objectKey: Any,
        keyBytes: ByteArray,
        value: Any,
        valueBytes: ByteArray,
        cluster: Cluster
    ): Int {
        val partitionInfoList = cluster.availablePartitionsForTopic(topic)
        val partitionCount = if (partitionInfoList.size == 0)  1 else partitionInfoList.size

        val key = objectKey as String
        return Math.abs(key.hashCode()) % partitionCount
    }

    override fun close() {}
}