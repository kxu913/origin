## Stage 1 : build with maven builder image with native capabilities
FROM container-registry.oracle.com/graalvm/native-image:17 AS build

ARG workspace

USER root
COPY $workspace/mvnw /code/mvnw
COPY $workspace/.mvn /code/.mvn
COPY $workspace/pom.xml /code/
WORKDIR /code

COPY $workspace/src /code/src
RUN ./mvnw package -DskipTests

## Stage 2 : create the final image, need a glibc or musl alpine
FROM frolvlad/alpine-glibc
COPY --from=build /code/target/demo /app/application

EXPOSE 8888
WORKDIR /app/
ENTRYPOINT [ "/app/application" ]
