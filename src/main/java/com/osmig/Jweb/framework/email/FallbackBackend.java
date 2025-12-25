package com.osmig.Jweb.framework.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * Email backend with automatic fallback between providers.
 * Tries each provider in order, falling back on rate limits or failures.
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * Mailer.configure(FallbackBackend.create()
 *     .add(new ResendBackend(resendKey, from))
 *     .add(new SendGridBackend(sendGridKey, from))
 *     .add(new BrevoBackend(brevoKey, from))
 *     .build());
 * }</pre>
 */
public class FallbackBackend implements Mailer.MailerBackend {
    private static final Logger log = LoggerFactory.getLogger(FallbackBackend.class);
    private final List<Mailer.MailerBackend> backends;

    private FallbackBackend(List<Mailer.MailerBackend> backends) {
        this.backends = backends;
    }

    public static Builder create() {
        return new Builder();
    }

    @Override
    public void send(Email email) throws Mailer.MailException {
        if (backends.isEmpty()) {
            throw new Mailer.MailException("No email backends configured");
        }

        Mailer.MailException lastException = null;
        for (int i = 0; i < backends.size(); i++) {
            Mailer.MailerBackend backend = backends.get(i);
            try {
                backend.send(email);
                return;
            } catch (ResendBackend.RateLimitException | SendGridBackend.RateLimitException |
                     BrevoBackend.RateLimitException e) {
                log.warn("Rate limit hit on {}, trying next provider", backend.getClass().getSimpleName());
                lastException = e;
            } catch (Mailer.MailException e) {
                log.warn("Failed with {}: {}, trying next", backend.getClass().getSimpleName(), e.getMessage());
                lastException = e;
            }
        }
        throw new Mailer.MailException("All email providers failed", lastException);
    }

    public static class Builder {
        private final List<Mailer.MailerBackend> backends = new ArrayList<>();

        public Builder add(Mailer.MailerBackend backend) {
            if (backend != null) backends.add(backend);
            return this;
        }

        public Builder resend(String apiKey, String from) {
            if (apiKey != null && !apiKey.isBlank()) {
                backends.add(new ResendBackend(apiKey, from));
            }
            return this;
        }

        public Builder sendGrid(String apiKey, String from) {
            if (apiKey != null && !apiKey.isBlank()) {
                backends.add(new SendGridBackend(apiKey, from));
            }
            return this;
        }

        public Builder brevo(String apiKey, String from) {
            if (apiKey != null && !apiKey.isBlank()) {
                backends.add(new BrevoBackend(apiKey, from));
            }
            return this;
        }

        public FallbackBackend build() {
            return new FallbackBackend(backends);
        }
    }
}
