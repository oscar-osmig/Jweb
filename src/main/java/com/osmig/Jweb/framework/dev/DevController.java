package com.osmig.Jweb.framework.dev;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller for development hot reload SSE endpoint.
 * Only active when hot-reload is enabled.
 */
@RestController
@RequestMapping("/__jweb_dev")
@ConditionalOnProperty(name = "jweb.dev.hot-reload", havingValue = "true")
public class DevController {

    /**
     * SSE endpoint for hot reload events.
     */
    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter events() {
        return DevServer.createEmitter();
    }

    /**
     * Manual reload trigger endpoint.
     */
    @GetMapping("/reload")
    public String reload() {
        DevServer.triggerReload();
        return "Reload triggered";
    }

    /**
     * Status endpoint.
     */
    @GetMapping("/status")
    public String status() {
        return "{\"enabled\":" + DevServer.isEnabled() + "}";
    }
}
