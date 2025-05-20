FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080
ENV TZ=Asia/Ho_Chi_Minh

ENTRYPOINT ["java", "-jar", "app.jar"]
