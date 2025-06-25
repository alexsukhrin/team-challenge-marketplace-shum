# Stage 1: Build the uberjar
FROM clojure:openjdk-17-tools-deps AS build
WORKDIR /app

# Copy project files
COPY deps.edn .
COPY src src
COPY resources resources
COPY config config
COPY config/prod.edn resources/config/prod.edn
COPY build.clj .

# Create the uberjar
RUN clj -X:uberjar
RUN cp target/app.jar app.jar

# Stage 2: Create the final production image
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# Copy the uberjar from the build stage
COPY --from=build /app/app.jar .
COPY --from=build /app/config config

# Expose the application port
EXPOSE 4000

# Set the entrypoint to run the application
# We also pass the ENV variable to use the production config
CMD ["java", "-DENV=prod", "-jar", "app.jar"] 