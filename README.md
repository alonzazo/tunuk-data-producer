# Tunuk IoT DataProducer - Community Version
## Description
IoT Data Producer for Public Transportation Monitoring

### Release notes


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
* Eager persistence
* Lazy persistance

Streaming modes:
* Micro-batches
  * Time interval trigger
* Streaming


Data filters:
* GPS JustOnce

## Roadmap
1. This data producer has to be:
   1. Secure
   2. Reliable
   3. Adaptable
   4. Configurable
