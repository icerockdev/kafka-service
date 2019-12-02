# Kafka

## Installation
````kotlin
// Append repository
repositories {
    maven { url = url("https://dl.bintray.com/icerockdev/backend") }
}

// Append dependency
implementation("com.icerockdev.service:kafka-service:0.0.1")
````

## Koin configure

### include dependency

````kotlin
// Porducer
    single(named("authTokenProducer")) {
        KafkaSender(
            appConf.getString("kafka.servers"),
            appConf.getString("kafka.clientId"),
            AuthTokenDto::class
        )
    }

// Executor
single {
    KafkaConsumerExecutionPool(Dispatchers.IO)
}
````

````kotlin
class Container : KoinComponent {
    val authTokenProducer: KafkaSender<AuthTokenDto> by inject(named("authTokenProducer"))
}
````

````
// application.conf
kafka {
    servers = ${KAFKA_BROKERS}
    clientId = "auth_service"
}
````

## Consumer definition
````kotlin
fun runConsumer(topic: String) {
    val consumer =
        KafkaConsumerFactory.createConsumer<AuthTokenDto>(
            servers = "server:port,server2:port",
            groupId = "consumer_group_name"
        )

    executor.runExecutor(
        consumer = consumer,
        topicList = Collections.singletonList(topic),
        pollWait = Duration.ofMillis(100)
    ) {
        this.forEach { record ->
            println("Record Key " + record.key())
            println("Record value " + record.value())
        }
        true
    }
}
````
### Required correct shutdown executors (AutoCloseable)

## Send data example
````kotlin
fun runProducer(sendMessageCount: Int, topic: String) {
    val sender = koinContainer.authTokenProducer

    val time = System.currentTimeMillis()
    for (index in time until time + sendMessageCount) {
        val t = AuthTokenDto(
            generateAuthToken(),
            "dddd",
            index.toInt(),
            DateTime.now()
        )

        sender.send(topic, t)
    }
}
````

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