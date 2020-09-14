

## Assumptions

* _An unactivated sensor_ : As the activation of the sensor is not part of the flow, the first event will define its state 
* _Typo on the collect sensor_ There is a small typo in the post measurement endpoint, which i guess could be to trigger communication. If it is the case i would rather focus on collaboratively design apis using contract first and or request for comments processes.

## Motivation of the implementation
I have done the following implementation to experiment a different way to mitigate some old problems:
* Kafka streams is a beautifully designed api, for who loves sql and which in most of the cases its abstracts leakys within the code when it does not drive the code as a huge procedure.
* All the event sourcing tools relies too much on impure functions/OO. Axon, lagom/cloudstate have the entities full of state which goes against the idea of [left fold](http://www.codebetter.com/gregyoung/2013/02/13/projections-1-the-theory/)  there is an old video on you with more details which i didnt find. but basicaly less state and more funre functions: (Command, State): Event and (Event, State): State, without mutability/hindden internal properties and state.


The definitions of these functions in java didnt look so good, but there is still hope. 

Also, apart from encapsulating the domain with these simple functions, in the adopted approach the aggregation (all alerts) and averages(time windows) is almost for free using the analytics kafka dsl. (Which i didnt manage to implement as of monday)


## Running

There is a docker compose which should provide the needed underlying systems:
```docker-compose -f docker-compose-infra-only.yaml  up```


In case running on linux, a simple `./gradlew bootRun` should do the trick
In case running on Macos, the docker machine ip should be used as spring  property *spring.kafka.bootstrap-servers*. 
Might be needed to change the composer file ADVERTISEDHOSTS.

A docker-compose.yaml with infra and service would solve this problem 

## Things to consider next:

- Aggregate the alerts
- Avgs with time windows
- Sensor events are published jackosn/json polymorphism. Json is questionable by itself if should be the preferred serialization format, however was chosen for visibility. The polymorphism for simplicity however should be replaced by a proper envelop / less technology dependent. One option could be cloudevents.io, which in early stages were quite problematic but should be safer / more stable now.


- Extended api definition restdocs / hal-hateos / error handling

