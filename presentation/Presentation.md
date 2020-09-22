
# Architecture overview


---

# But, Before

* What is the problem again?
* How can we understand the problem talking in the same language as product

* Event Storming / eventmodeling.org

* Created by Alberto, an experienced UML consultant
  - Bring together Domain experts and Developers
  - Collaboratively
  - Simply
  - Interactive and Iterative
  - Throw away, try again next week

---

# Represented as events and projections

![Event storming](event_storming.png)

---

# Immutability as foundation

## Definition
```Scala
(Command aCommand, State currentState): Event
```

```Scala
(Event anEvent, State currentState): State
```

## Implementation

* Just a fold operation.

* Continuously running, without need snapshot

* Encapsulated in an UnitOfWork

---

# Demo
 
 
---

[![asciicast](https://asciinema.org/a/8ZL0EHLs0Fyezf7ljPcXjoG8R.png)](https://asciinema.org/a/8ZL0EHLs0Fyezf7ljPcXjoG8R
)

---

[![asciicast](https://asciinema.org/a/wNAdkZuYKo6gBnndZuTMnvKtj.png)](https://asciinema.org/a/wNAdkZuYKo6gBnndZuTMnvKtj
)


---
# Why?



![cloudstate](cloudstatex800.png)

---
# Why 2

![axon](axonx800.png)

---
# Why 3

![kafka-streams](kafka-streamsx800.png)

---
# Technologies

* Java 14
  - Records, 
  - Switch expressions
  - If instanceOf pattern matching
  
* Spring

* Kafka & Kafka streams

---

# Before production-ready:

* Static analysis 

* Pipelines

* Metrics / Monitoring / Logging

* Kafka Streams Limitations
  - Cooperative reassignment / stand-by
  - Sharded/Partitioned data
  - Schemas

  

---
# Microservice Architecture

* Don't
* Don't
* Driven by the business / conways law represented by bounded contexts
* Considerations about Cohesion / Deployment
* Sharing immutable behaviour
* Mutable state should be internal, not exposed
* Lets go  back to the event storming ;) ... and for discussion