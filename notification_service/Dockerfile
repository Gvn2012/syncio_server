FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY . .

RUN chmod +x mvnw && \
    ./mvnw clean package -pl notification_service -am -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/notification_service/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]