# Reactive Event-Driven Microservices & Saga Pattern

![Learn Kafka From Scratch](.doc/kafka.png)

This course is specifically designed for senior or staff-level engineers who are interested in learning about **Event-Driven Microservices & Saga Pattern** , and various architectural patterns using technologies such as **Spring Cloud Stream**, Kafka, and Java Reactive Programming. 

By the end of the course, participants will gain a deep understanding and comfort with the following patterns: 

- Saga Choreography Pattern
- Saga Orchestrator Pattern
- Transactional Outbox Pattern
- Fan-Out / Fan-In
- Content-Based Routing / Dynamic Routing

Here is what we will do in this course:

- The course begins by exploring Spring Cloud Stream, a framework for developing Event-Driven Microservices. Participants will learn how to use the Reactive Kafka binder and practice producing, consuming, processing, and acknowledging messages using Java Reactive and functional interfaces. Integration tests using Embedded Kafka will also be covered.
- Next, participants will delve into Stream Bridge, a utility for sending arbitrary messages to a Kafka Topic. Stream Bridge enables routing messages based on content, achieving dynamic routing, and even acting as a Dead-Letter-Topic producer during error handling.
- The course then progresses to designing a complex application involving multiple microservices. Participants will learn how to achieve a complex workflow that involves all the services, with a focus on implementing the Saga Pattern. The Saga Orchestrator and Saga Choreography styles will be explored, where participants will understand the role of a central coordinator or the observation and reaction of events among the saga participants.
- Finally, the course covers the Transactional Outbox Pattern, which addresses reliable message sending to a Kafka Topic. The pattern involves the use of an 'Outbox' table acting as a proxy Kafka topic. Messages intended for the Kafka topic are first inserted into this table as part of the application's database transaction. Periodic querying of the table ensures messages are sent, marked as 'Sent,' and not sent again.