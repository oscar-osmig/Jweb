package com.osmig.Jweb.app.pages.tryit;

import static com.osmig.Jweb.framework.js.Actions.*;

/** JavaScript handlers for Try It page forms. */
public final class TryItScripts {
    private TryItScripts() {}

    public static String formHandlers() {
        return script()
            .withHelpers()
            .add(onSubmit("request-form")
                .loading("Sending...")
                .post("/api/try-it/request").withFormData()
                .ok(whenResponse("success")
                    .then(all(
                        showMessage("request-message").fromResponse("message").success(),
                        resetForm("request-form")
                    ))
                    .otherwise(whenResponse("warning")
                        .then(showMessage("request-message").fromResponse("message").warning())
                        .otherwise(showMessage("request-message").fromResponse("message").error())))
                .fail(showMessage("request-message").error("Network error")))
            .add(onSubmit("download-form")
                .loading("Validating...")
                .get("/api/try-it/validate").withFormData()
                .ok(whenResponse("valid")
                    .then(all(
                        download("/api/try-it/download").param("token").param("email").as("jweb-starter.zip"),
                        showMessage("download-message").success("Download started!")
                    ))
                    .otherwise(showMessage("download-message").fromResponse("error").error()))
                .fail(showMessage("download-message").error("Network error")))
            .build();
    }
}
