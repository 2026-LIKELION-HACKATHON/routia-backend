FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew

COPY src ./src
RUN ./gradlew bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre-jammy AS runtime

ENV TZ=Asia/Seoul
WORKDIR /app

RUN groupadd --system --gid 10001 routia \
    && useradd --system --uid 10001 --gid routia --home-dir /app --shell /usr/sbin/nologin routia

COPY --from=builder --chown=routia:routia /app/build/libs/app.jar /app/app.jar

USER routia:routia
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
