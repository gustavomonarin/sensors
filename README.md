# Sensors
A kafka backed, immutability experimentation, iot co2 sensor assessment with the following endpoints:

```POST(/{uuid}/mesurements)```: Collects a new measurement.

```GET(/{uuid})```: Current sensor state.

```GET(/{uuid}/metrics)```: The metrics (max, avg) for the last 30 days.

```GET(/{uuid}/alerts)```: The aggregated co2 level raised alerts. 

:gift: [Presentation](https://gustavomonarin.github.io/sensors/presentation/Presentation.html)

## Motivation of the implementation
I have done the following implementation to experiment a different way to mitigate some old problems:
* Kafka streams is a beautifully designed api, for who loves sql and which in most of the cases pollutes the domain
 leaking abstractions within the code when it does not drive the code as a huge procedure.
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