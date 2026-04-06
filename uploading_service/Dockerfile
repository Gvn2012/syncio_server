FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw install -N -DskipTests
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml
COPY shared shared
COPY image_uploading_service image_uploading_service

RUN ./mvnw clean install -f shared/pom.xml -DskipTests && \
    ./mvnw clean package -f image_uploading_service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/image_uploading_service/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]