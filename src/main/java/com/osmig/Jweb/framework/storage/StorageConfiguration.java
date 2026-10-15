package com.osmig.Jweb.framework.storage;

import com.osmig.Jweb.framework.util.Log;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import java.time.Duration;

/**
 * Picks the {@link jweb.Storage} store at startup — a {@code Storage.Store}
 * bean when the app defines one, S3 when {@code jweb.storage.s3.bucket} is
 * set, the local directory {@code jweb.storage.dir} otherwise — and mounts
 * the route that serves the files at {@code jweb.storage.path} unless
 * {@code jweb.storage.serve} is false.
 */
@Configuration
public class StorageConfiguration {

    @Value("${jweb.storage.dir:storage}")
    private String dir;

    @Value("${jweb.storage.path:" + jweb.Storage.DEFAULT_PATH + "}")
    private String path;

    @Value("${jweb.storage.access:public}")
    private String access;

    @Value("${jweb.storage.url-ttl:1h}")
    private Duration urlTtl;

    @Value("${jweb.storage.cache-control:private, max-age=3600}")
    private String cacheControl;

    @Value("${jweb.storage.serve:true}")
    private boolean serve;

    @Value("${jweb.storage.s3.bucket:}")
    private String bucket;

    @Value("${jweb.storage.s3.endpoint:}")
    private String endpoint;

    @Value("${jweb.storage.s3.region:us-east-1}")
    private String region;

    @Value("${jweb.storage.s3.access-key:}")
    private String accessKey;

    @Value("${jweb.storage.s3.secret-key:}")
    private String secretKey;

    @Value("${jweb.storage.s3.path-style:#{null}}")
    private Boolean pathStyle;

    @Value("${jweb.storage.s3.public-url:}")
    private String publicUrl;

    /** The URL settings apply before any route or page asks for a URL. */
    @PostConstruct
    void settings() {
        jweb.Storage.path(path);
        jweb.Storage.signedOnly("signed".equalsIgnoreCase(access.trim()));
        jweb.Storage.urlTtl(urlTtl);
        jweb.Storage.cacheControl(cacheControl == null || cacheControl.isBlank() ? null : cacheControl);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 110)
    public ApplicationRunner jwebStorageStore(ObjectProvider<jweb.Storage.Store> stores) {
        return args -> {
            jweb.Storage.Store custom = stores.getIfUnique();
            if (custom != null) {
                jweb.Storage.store(custom);
                Log.info("jweb.Storage store: {}", custom);
            } else if (bucket != null && !bucket.isBlank()) {
                if (accessKey.isBlank() || secretKey.isBlank()) {
                    Log.error("jweb.storage.s3.bucket is set but jweb.storage.s3.access-key / secret-key are not; "
                        + "files stay in {}", dir);
                    jweb.Storage.store(new jweb.Storage.Local(dir));
                    return;
                }
                boolean pathStyleAddressing = pathStyle != null ? pathStyle : !endpoint.isBlank();
                jweb.Storage.S3 s3 = new jweb.Storage.S3(endpoint, region, bucket, accessKey, secretKey, pathStyleAddressing)
                    .publicUrl(publicUrl);
                jweb.Storage.store(s3);
                Log.info("jweb.Storage store: {}{}", s3, publicUrl.isBlank() ? "" : " public at " + publicUrl);
            } else {
                jweb.Storage.Local local = new jweb.Storage.Local(dir);
                jweb.Storage.store(local);
                Log.info("jweb.Storage store: {} served at {}{}", local, jweb.Storage.path(),
                    jweb.Storage.signedOnly() ? " (signed URLs only)" : "");
            }
        };
    }

    /** The serving route, mounted after the app's own routes. */
    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    public jweb.JWebRoutes jwebStorageRoutes() {
        return app -> {
            if (!serve) return;
            app.get(jweb.Storage.path() + "/*", jweb.Storage.serve());
        };
    }
}
