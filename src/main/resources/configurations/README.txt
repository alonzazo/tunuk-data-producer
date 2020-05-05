DELL_3003_RXTX
producer.serialport=/dev/ttyUSB0
databus.mode=streaming

ADAFRUIT_ULTIMATE
producer.serialport=/dev/ttyUSB0
databus.mode=streaming

HELLA_APC_ECO_RS485
producer.doorid=1
producer.ipaddress=192.168.8.221
producer.port=10076
databus.mode=microbatch
databus.interval=1000

HELLA_PUSH_NOTIFICATION
producer.port=80
producer.devices=B3:23:A2:23:AD:CC,B3:21:12:45:32:23
databus.mode=microbatch
databus.interval=5000

HELLA_APC_ECO_RS485
producer.doorid=2
producer.ipaddress=192.168.8.221
producer.port=10076
databus.mode=microbatch
databus.interval=1000

HONGDIAN_H8922S
producer.port=2502

TEST_GPS
databus.mode=microbatch
databus.interval=1000

TEST_APC
databus.mode=microbatch
databus.interval=1000