FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY auth_service ./auth_service

RUN chmod +x ./auth_service/mvnw && \
    ./auth_service/mvnw -f ./auth_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/auth_service/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]