FROM eclipse-temurin:21-jdk AS build

WORKDIR /build

COPY pom.xml .
COPY user_service ./user_service

RUN chmod +x ./user_service/mvnw && \
    ./user_service/mvnw -f ./user_service/pom.xml clean package -Dmaven.test.skip=true

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /build/user_service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "app.jar"]