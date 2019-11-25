/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package com.icerockdev.service.kafka

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.util.StdDateFormat
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ObjectDeserializer<T : Any>(private val type: TypeReference<T>): Deserializer<T> {
    private val logger: Logger = LoggerFactory.getLogger(ObjectDeserializer::class.java)
    private val mapper = ObjectMapper().apply {
        configure(SerializationFeature.INDENT_OUTPUT, true)
        enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        registerModule(JodaModule())
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        dateFormat = StdDateFormat()
        registerKotlinModule()
    }

    override fun configure(configs: Map<String, *>?, isKey: Boolean) {}

    override fun deserialize(topic: String, data: ByteArray): T? {
        var `object`: T? = null
        try {
            `object` = mapper.readValue(data, type)
        } catch (exception: Exception) {
            logger.error("Error in deserializing bytes ${exception.localizedMessage}", exception)
        }

        return `object`
    }

    override fun close() {}
}