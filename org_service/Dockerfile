FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY org_service ./org_service

RUN chmod +x ./org_service/mvnw && \
    ./org_service/mvnw -f ./org_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/org_service/target/*.jar app.jar

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]