# Kafka

## Installation
````kotlin
// Append repository
repositories {
    maven { url = url("https://dl.bintray.com/icerockdev/backend") }
}

// Append dependency
implementation("com.icerockdev.service:kafka-service:0.0.2")
````

## Koin configure

### include dependency

````kotlin
// Producer
single(TestProducerService(
    appConf.getString("kafka.servers"),
    appConf.getString("kafka.clientId"),
    "topicName"
))

// Consumer
single(TestKafkaConsumer(
    appConf.getString("kafka.servers"),
    appConf.getString("kafka.groupId"),
    appConf.getString("kafka.clientId")
))

// Executor
single {
    KafkaConsumerExecutionPool(Dispatchers.IO)
}
````

````
// application.conf
kafka {
    servers = ${KAFKA_BROKERS}
    clientId = "auth_service"
}
````
## Producer definition
````kotlin
class TestProducerService(servers: String, clientId: String, private val topic: String) : AutoCloseable {
    private val producer = KafkaProducerBuilder()
//        .applyTransactional(KAFKA_TRANSACTION_ID) // supported only for 3 brokers and more
        .applyIdempotence()
        .applyTimeout()
        .applyBuffering()
        .build<Long, String>(
            servers = servers,
            clientId = clientId,
            keySerializer = LongSerializer(),
            valueSerializer = ObjectSerializer()
        )

    fun sendData(model: String): Boolean {
        val time = System.currentTimeMillis()
        return KafkaSender.send(producer, topic, time, model)
    }

    fun sendAsyncData(model: String) {
        val time = System.currentTimeMillis()
        KafkaSender.sendAsync(producer, topic, time, model)
    }

    override fun close() {
        producer.flush()
        producer.close()
    }
}
````

## Consumer definition
````kotlin
class TestKafkaConsumer(servers: String, groupId: String, clientId: String) : IKafkaConsumer {

    private val consumer =
        KafkaConsumerBuilder()
            .applyReadOpt()
            .applyIsolation(KafkaConsumerBuilder.IsolationLevel.READ_COMMITTED)
            .apply {
                with(props) {
                    this[ConsumerConfig.FETCH_MAX_BYTES_CONFIG] = 50 * 1024 * 1024
                    this[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = KafkaConsumerBuilder.Offset.LATEST.value
                }
            }
            .build<String, String>(
                servers,
                groupId,
                clientId,
                StringDeserializer(),
                ObjectDeserializer(jacksonTypeRef())
            )


    override fun run(executor: KafkaConsumerExecutionPool, topic: String) {
        executor.runExecutor(
            consumer = consumer,
            topicList = Collections.singletonList(topic),
            pollWait = Duration.ofMillis(100)
        ) {
            this.forEach { record ->
                // apply values
                println("Read value: ${record.value()}")
            }
            true
        }

    }

    override fun close() {
        consumer.close()
    }
}
````
### Required correct shutdown executors (AutoCloseable)

### Warning! Do not change stored data format (or save backward compatibility)

## Contributing
All development (both new features and bug fixes) is performed in the `develop` branch. This way `master` always contains the sources of the most recently released version. Please send PRs with bug fixes to the `develop` branch. Documentation fixes in the markdown files are an exception to this rule. They are updated directly in `master`.

The `develop` branch is pushed to `master` on release.

For more details on contributing please see the [contributing guide](CONTRIBUTING.md).

## License
        
    Copyright 2019 IceRock MAG Inc.
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.