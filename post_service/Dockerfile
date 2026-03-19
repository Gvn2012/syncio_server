FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY post_service ./post_service

RUN chmod +x ./post_service/mvnw && \
    ./post_service/mvnw -f ./post_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/post_service/target/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java", "-jar", "app.jar"]