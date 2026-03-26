FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY permission_service ./permission_service

RUN chmod +x ./permission_service/mvnw && \
    ./permission_service/mvnw -f ./permission_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/permission_service/target/*.jar app.jar

EXPOSE 8088

ENTRYPOINT ["java", "-jar", "app.jar"]