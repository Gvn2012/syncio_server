FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY discovery_service ./discovery_service

RUN chmod +x ./discovery_service/mvnw && \
    ./discovery_service/mvnw -f ./discovery_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/discovery_service/target/*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]