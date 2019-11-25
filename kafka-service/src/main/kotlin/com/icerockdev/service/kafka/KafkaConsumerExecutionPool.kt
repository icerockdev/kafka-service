/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import kotlinx.coroutines.*
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

class KafkaConsumerExecutionPool(coroutineContext: CoroutineContext) : AutoCloseable {

    val logger: Logger = LoggerFactory.getLogger(KafkaConsumerExecutionPool::class.java)

    var isActiveJob = true
        private set

    val coroutineScope = CoroutineScope(coroutineContext + SupervisorJob())
    val jobSet: MutableSet<Job> = Collections.newSetFromMap(ConcurrentHashMap<Job, Boolean>())

    inline fun <reified V> runExecutor(
        consumer: KafkaConsumer<String, V>,
        topicList: List<String>,
        pollWait: Duration = Duration.ofMillis(100),
        crossinline block: suspend ConsumerRecords<String, V>.() -> Boolean
    ) {
        consumer.subscribe(topicList)
        logger.info("Consumer subscribed: $topicList")

        val job = coroutineScope.launch {
            while (isActiveJob) {
                try {
                    val consumerRecords = consumer.poll(pollWait)
                    if (consumerRecords.count() == 0) {
                        continue
                    }

                    if (consumerRecords.block()) {
                        consumer.commitAsync()
                    }
                } catch (e: Throwable) {
                    logger.error(e.localizedMessage, e)
                    continue
                }
            }
            logger.info("Consumer executor closed: $topicList")
        }
        jobSet.add(job)
    }

    /**
     * Waiting all coroutine
     */
    override fun close() {
        isActiveJob = false
        runBlocking {
            for (job in jobSet) {
                if (job.isActive) {
                    job.join()
                }
            }
        }
        coroutineScope.cancel()
        logger.info("KafkaConsumerExecutionPool stopped")
    }
}