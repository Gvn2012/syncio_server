FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY friend_service ./friend_service

RUN chmod +x ./friend_service/mvnw && \
    ./friend_service/mvnw -f ./friend_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/friend_service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]