FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY notification_service ./notification_service

RUN chmod +x ./notification_service/mvnw && \
    ./notification_service/mvnw -f ./notification_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/notification_service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]