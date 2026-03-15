FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY api_gateway ./api_gateway

RUN chmod +x ./api_gateway/mvnw && \
    ./api_gateway/mvnw -f ./api_gateway/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/api_gateway/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]