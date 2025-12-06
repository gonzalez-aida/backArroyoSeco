FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . /app
RUN mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar /app/booking.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","booking.jar"]