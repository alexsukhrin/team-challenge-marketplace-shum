# Stage 1: Build the uberjar
FROM clojure:tools-deps-1.11.1 AS build
WORKDIR /app

# Copy project files
COPY deps.edn .
COPY src src
COPY resources resources
COPY config config

# Create the uberjar
RUN clj -X:uberjar

# Stage 2: Create the final production image
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copy the uberjar from the build stage
COPY --from=build /app/marketplace-shum.jar .

# Expose the application port
EXPOSE 3000

# Set the entrypoint to run the application
# We also pass the ENV variable to use the production config
CMD ["java", "-DENV=prod", "-jar", "marketplace-shum.jar"] 