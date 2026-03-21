FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY . .

RUN chmod +x mvnw && \
    ./mvnw clean package -pl user_service -am -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/user_service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]