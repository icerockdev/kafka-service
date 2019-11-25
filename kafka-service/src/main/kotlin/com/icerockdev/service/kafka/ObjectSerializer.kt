/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class ObjectSerializer<T : Any>(): Serializer<T> {
    private val logger: Logger = LoggerFactory.getLogger(ObjectSerializer::class.java)
    private val mapper = ObjectMapper().apply {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        dateFormat = StdDateFormat()
        registerModule(JodaModule())
        registerKotlinModule()
    }

    override fun configure(configs: Map<String, *>?, isKey: Boolean) {}

    override fun serialize(topic: String?, data: T): ByteArray {
        val retVal: ByteArray = byteArrayOf()
        try {
            return mapper.writeValueAsString(data).toByteArray()
        } catch (exception: Exception) {
            logger.error("Error in serializing object ${exception.localizedMessage}", exception)
        }
        return retVal
    }

    override fun close() {}
}