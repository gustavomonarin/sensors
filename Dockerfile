# Multi stage docker file to centralize building, analysis and deployable artifacts. ref: https://docs.docker.com/develop/develop-images/multistage-build/

### Builder image, used only to build the application
FROM openjdk:14-ea-slim as build

WORKDIR /workspace/app

ADD gradle/ gradle/
ADD gradlew ./
ADD settings.gradle ./
ADD build.gradle ./

ADD src/ src/

RUN ./gradlew build
RUN mkdir -p build/dependency && (cd build/dependency; jar -xf ../libs/*.jar)


### Final docker image, containing only the necessary for deploy with minimal and optimal layers
#find slimner :)
FROM openjdk:14-ea-slim

VOLUME /tmp

ARG DEPENDENCY=/workspace/app/build/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","--enable-preview", "-cp","app:app/lib/*","com.acme.sensors.SensorsApplication"]