FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw install -N -DskipTests
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml
COPY shared shared
COPY notification_service notification_service

RUN ./mvnw clean install -f shared/pom.xml -DskipTests && \
    ./mvnw clean package -f notification_service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/notification_service/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]