# =====================================================================
# Dockerfile de production pour l'API ColocImmo (Spring Boot / JHipster)
# A deployer sur Railway (Root Directory = backend)
# =====================================================================

# ---- Etape 1 : build du .jar avec le profil prod ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY . .
RUN chmod +x mvnw && ./mvnw -B -ntp -Pprod -DskipTests -Dmodernizer.skip=true clean package

# ---- Etape 2 : image d'execution legere ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# ${PORT} est fourni par Railway ; 8080 par defaut en local
CMD ["sh","-c","java -Djava.security.egd=file:/dev/./urandom -jar app.jar --server.port=${PORT:-8080}"]

