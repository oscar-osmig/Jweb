# Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY src/ src/

# /docs/tell assembles the documentation set from the classpath at runtime, and
# the copy-readme execution in pom.xml reads these from the project ROOT — not
# from src/. Leave them out of the build context and maven-resources quietly
# copies nothing, the endpoint throws on the first missing guide, and the docs
# page returns 500. That is exactly how it shipped once.
COPY README.md dsl-simplification.md dsl-simplification-3.md ./
COPY readme/ readme/

RUN ./mvnw package -DskipTests -B -Pdemo

# The build skips tests, so DocsTellTest cannot catch a missing guide here.
# Fail the image build rather than let the endpoint discover it in production.
# The list comes from DocsTell itself: every "readme/..." path it serves must
# be on the classpath, so a guide added there but not COPYed above fails here
# instead of shipping a 500 (dsl-simplification-3.md did exactly that once).
RUN for f in $(grep -o '"readme/[^"]*"' src/main/java/com/osmig/Jweb/app/docs/DocsTell.java | tr -d '"'); do \
      test -s "target/classes/$f" || { echo "ERROR: target/classes/$f missing — documentation is not in the build context"; exit 1; }; \
    done

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*-exec.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
