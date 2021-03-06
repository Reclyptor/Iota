FROM openjdk:8-jdk-alpine
ENV MYSQL_ENDPOINT=jdbc:mysql://root:toor@host.docker.internal:3306/telemetry
COPY build/libs/iota-0.0.1-SNAPSHOT.jar iota.jar
ENTRYPOINT ["java", "-jar", "iota.jar"]