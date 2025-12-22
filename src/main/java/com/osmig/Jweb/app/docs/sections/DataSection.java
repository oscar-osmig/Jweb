package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;
import static com.osmig.Jweb.app.docs.DocExamples.*;

public final class DataSection {
    private DataSection() {}

    public static Element render() {
        return section(
            title("Data & JPA"),
            text("JWeb integrates with Spring Data JPA for database access. " +
                 "Define entities, repositories, and let Spring handle the rest."),

            subtitle("Entity Definition"),
            code(DATA_ENTITY),

            subtitle("Repository Interface"),
            text("Extend JpaRepository to get CRUD operations automatically:"),
            code(DATA_REPOSITORY),

            subtitle("Using in Routes"),
            code(DATA_USAGE),

            subtitle("Configuration"),
            text("Configure database settings in application.yaml:"),
            code(DATA_CONFIG),

            subtitle("H2 Console"),
            text("For development, H2 provides an in-browser database console at /h2-console " +
                 "when enabled. Great for debugging and testing queries."),

            list(
                "H2 - In-memory database for development",
                "PostgreSQL - Production-ready relational database",
                "MySQL - Popular relational database",
                "MongoDB - Document database (optional dependency)"
            )
        );
    }
}
