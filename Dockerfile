FROM maven:3.9.6-eclipse-temurin-17 AS builder

COPY src /app/src
COPY pom.xml /app

WORKDIR /app

RUN mvn clean install

FROM eclipse-temurin:17

LABEL org.opencontainers.image.title="stripe-payment-service" \
                  org.opencontainers.image.description="Serviço de pagamento com Stripe" \
                  org.opencontainers.image.authors="Gabriel <gabriel@empresa.com>" \
                  org.opencontainers.image.version="1.0.0"

COPY --from=builder /app/target/stripe-payment-0.0.1-SNAPSHOT.jar /app/app.jar
WORKDIR /app

EXPOSE 8080

CMD ["java","-jar","app.jar"]