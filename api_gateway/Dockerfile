FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw install -N -DskipTests
RUN sed -i '/<modules>/,/<\/modules>/d' pom.xml
COPY shared shared
COPY api_gateway api_gateway

RUN ./mvnw clean install -f shared/pom.xml -DskipTests && \
    ./mvnw clean package -f api_gateway/pom.xml -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/api_gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]