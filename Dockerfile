# ============================================================
# Stage 1 — Build : compile + package avec Maven
# ============================================================
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /build

# Copier uniquement les fichiers nécessaires à la résolution des dépendances
# (exploite le cache Docker si le code source change mais pas le pom.xml)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Télécharger les dépendances sans compiler le code
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copier le reste du code source
COPY src/ src/

# Construire le JAR en sautant les tests
RUN ./mvnw package -DskipTests -B

# ============================================================
# Stage 2 — Runtime : image légère sans Maven ni JDK
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# Sécurité : exécuter l'app avec un utilisateur non-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copier uniquement le JAR produit par le stage de build
COPY --from=builder /build/target/*.jar app.jar

# Donner les droits à l'utilisateur applicatif
RUN chown appuser:appgroup app.jar

USER appuser

# Port exposé (défini dans application.properties)
EXPOSE 8080

# Variables d'environnement configurables au démarrage du conteneur
# (les valeurs par défaut viennent d'application.properties)
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Point d'entrée
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
