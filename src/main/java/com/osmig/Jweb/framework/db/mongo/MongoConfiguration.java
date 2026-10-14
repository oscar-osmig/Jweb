package com.osmig.Jweb.framework.db.mongo;

import com.osmig.Jweb.framework.util.Log;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connects {@link jweb.Mongo} at startup when {@code jweb.data.enabled} is
 * {@code true} — URI and database from {@code jweb.data.mongo.uri} and
 * {@code jweb.data.mongo.database}.
 *
 * <p>The MongoDB driver is an optional dependency: this class is only
 * considered when {@code mongodb-driver-sync} is on the classpath, so an app
 * that uses SQL, or no database, never loads a Mongo type.
 * {@link com.osmig.Jweb.framework.config.JWebConfiguration} says what to add
 * when the property is on and the driver is not.</p>
 */
@Configuration
@ConditionalOnClass(name = "com.mongodb.client.MongoClient")
@ConditionalOnProperty(name = "jweb.data.enabled", havingValue = "true")
public class MongoConfiguration {

    @Value("${jweb.data.mongo.uri:mongodb://localhost:27017}")
    private String mongoUri;

    @Value("${jweb.data.mongo.database:myapp}")
    private String mongoDatabase;

    @Bean
    public ApplicationRunner mongoInitializer() {
        return args -> {
            Mongo.connect(mongoUri, mongoDatabase);
            Log.info("jweb.Mongo connected to database '{}'", mongoDatabase);
        };
    }
}
