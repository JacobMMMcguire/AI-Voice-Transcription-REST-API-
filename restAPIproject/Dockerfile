# --- Stage 1: Build the Application (Builder Stage) ---
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app
# Copy the project files (pom.xml first to optimize layer caching)
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline 

COPY src/ ./src/
RUN mvn package

# --- Stage 2: Create the Custom Runtime (Final Image) ---
FROM eclipse-temurin:21 AS jre-build

RUN $JAVA_HOME/bin/jlink \
         --add-modules java.base,java.net.http,jdk.httpserver,java.logging,java.management,jdk.crypto.ec,java.security.jgss \
         --strip-debug \
         --no-man-pages \
         --no-header-files \
         --compress=2 \
         --output /javaruntime

FROM debian:buster-slim
ENV JAVA_HOME=/opt/java/openjdk
ENV PATH="${JAVA_HOME}/bin:${PATH}"
COPY --from=jre-build /javaruntime $JAVA_HOME

RUN mkdir /opt/app
WORKDIR /opt/app

COPY --from=builder /app/target/restAPIproject-shaded.jar .

CMD ["java", "-cp", "/opt/app/restAPIproject-shaded.jar", "RestAPIproject.RestAPI"]
