FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw install -N -DskipTests
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml
COPY shared shared
COPY grpc_common grpc_common
COPY org_service org_service

RUN ./mvnw clean install -f shared/pom.xml -DskipTests && \
    ./mvnw clean install -f grpc_common/pom.xml -DskipTests && \
    ./mvnw clean package -f org_service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/org_service/target/*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]