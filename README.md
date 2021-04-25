# Thomson-kafka-serializer

Kafka for reading and transforming Thomson router log data.
Thomson logger produces 4 different types of logging strings that the program parses and outputs to kafka producer

Need to handle 4 cases
```bash
"Jan 12 17:34:12 2021 SYSLOG[0]: message repeated 2 times: [ [Host 192.168.0.1] UDP 192.168.0.14,57621 --> 192.168.0.255,57621 ALLOW: Inbound access request ]"
"Jan 12 17:34:12 2021 SYSLOG[0]: [Host 192.168.0.1] ICMP (type 3) 24.193.175.197 --> 82.181.71.193 DENY: Firewall interface access request"
"Jan  9 18:29:30 2021 SYSLOG[0]: [Host 192.168.0.1] TCP 161.97.78.236,45362 --> 82.181.71.193,3389 DENY: Firewall interface access request"
"Jan 31 12:00:21 2021 SYSLOG[0]: [Host 192.168.0.1]  Time Of Day established "
```

## Instructions

- To run tests
```./gradlew test```

- To run application
```./gradlew run```