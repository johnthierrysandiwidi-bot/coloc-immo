# --- Build ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -B -ntp -Pprod -DskipTests clean package

# --- Run ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["sh","-c","java -Djava.security.egd=file:/dev/./urandom -jar app.jar --server.port=${PORT:-8080}"]