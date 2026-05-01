FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw install -N -DskipTests
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml
COPY shared shared
COPY grpc_common grpc_common
COPY call_service call_service

RUN ./mvnw clean install -f shared/pom.xml -DskipTests && \
    ./mvnw clean install -f grpc_common/pom.xml -DskipTests && \
    ./mvnw clean package -f call_service/pom.xml -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/call_service/target/*.jar app.jar
EXPOSE 8092
ENTRYPOINT ["java", "-jar", "app.jar"]
