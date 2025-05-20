FROM maven:3.8.4-openjdk-17-slim AS builder

WORKDIR /build
COPY . .
RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app
# Install curl for health checks
RUN apt-get update && apt-get install -y curl && apt-get clean

COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8761
ENV TZ=Asia/Ho_Chi_Minh

ENTRYPOINT ["java", "-jar", "app.jar"]
