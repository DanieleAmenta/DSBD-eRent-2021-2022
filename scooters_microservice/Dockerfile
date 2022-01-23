FROM maven:3-jdk-8 as builder
WORKDIR /project
COPY shared_entities/ shared_entities
WORKDIR /project/shared_entities
RUN mvn install
WORKDIR /project
COPY ./scooters_microservice/pom.xml ./pom.xml
RUN mvn dependency:go-offline -B
COPY ./scooters_microservice/src ./src
RUN mvn package

FROM java:8-alpine
WORKDIR /app
COPY --from=builder /project/target/scooters_microservice-0.0.1-SNAPSHOT.jar ./scooters_microservice.jar
#ENTRYPOINT ["/bin/sh", "-c"]
CMD java -jar scooters_microservice.jar