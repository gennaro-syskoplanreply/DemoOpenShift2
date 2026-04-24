####
# This Dockerfile is used in order to build a container that runs the Spring Boot application
#
# Build the image with:
#
# docker build -f docker/Dockerfile -t springboot/sample-demo .
#
# Then run the container using:
#
# docker run -i --rm -p 8081:8081 springboot/sample-demo
####
#FROM registry.access.redhat.com/ubi8/openjdk-17:1.23-4.1771813014 AS builder
FROM registry.access.redhat.com/ubi8/openjdk-21:latest AS builder

# Build dependency offline to streamline build
RUN mkdir project
WORKDIR /home/jboss/project
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src src
RUN mvn package -Dmaven.test.skip=true

# Usa il nome fisso app.jar
RUN mv target/app.jar target/export-run-artifact.jar

FROM registry.access.redhat.com/ubi8/openjdk-21-runtime:latest
COPY --from=builder /home/jboss/project/target/export-run-artifact.jar /deployments/export-run-artifact.jar
EXPOSE 8081
ENTRYPOINT ["/opt/jboss/container/java/run/run-java.sh", "--server.port=8081"]