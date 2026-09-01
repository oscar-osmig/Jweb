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
COPY README.md dsl-simplification.md ./
COPY readme/ readme/

RUN ./mvnw package -DskipTests -B -Pdemo

# The build skips tests, so DocsTellTest cannot catch a missing guide here.
# Fail the image build rather than let the endpoint discover it in production.
RUN for f in target/classes/readme/README.md \
             target/classes/readme/dsl-simplification.md \
             target/classes/readme/guides/html-dsl.md \
             target/classes/readme/guides/known-issues.md; do \
      test -s "$f" || { echo "ERROR: $f missing — documentation is not in the build context"; exit 1; }; \
    done

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*-exec.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
