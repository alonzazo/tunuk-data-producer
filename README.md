# Tunuk IoT DataProducer - Community Version
## Description
IoT Data Producer for Public Transportation Monitoring
### Integrations

### Release logs


## Setting up

## Architecture
![Architecture](https://user-images.githubusercontent.com/18171087/124397284-4987ae80-dccc-11eb-9790-fc810cf2ad63.png)

**Design pattern:** Event bus

Producer controllers supported:
* Hella APC ECO-RS485
* Hella PushNotification
* Dell 3003 RXTX GPS
* Adafruit Ultimate GPS
* Hongdian H8922S
* TestGPS (Simulated data generator)
* TestAPC (Simulated data generator)

Subscribers controllers supported:
* Apache Kafka
* Amazon AWS

Fail tolerance systems supported:
* Persistencia pesimista (Eager)
* Persistencia optimista (Lazy)

Streaming modes:
* Micro-batches
  * Time interval trigger
* Streaming


Data filters:
* GPS JustOnce

## Roadmap

