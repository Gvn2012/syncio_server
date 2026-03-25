FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY relationship_service ./relationship_service

RUN chmod +x ./relationship_service/mvnw && \
    ./relationship_service/mvnw -f ./relationship_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/relationship_service/target/*.jar app.jar

EXPOSE 8087

ENTRYPOINT ["java", "-jar", "app.jar"]