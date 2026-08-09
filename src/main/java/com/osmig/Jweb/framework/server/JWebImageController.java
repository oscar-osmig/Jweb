package com.osmig.Jweb.framework.server;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Built-in image optimization: resizes static images on the fly and caches
 * the result — the Next.js/Astro feature, with zero dependencies (ImageIO).
 *
 * <p>{@code GET /jweb/img?src=/static/photo.jpg&w=400} serves photo.jpg
 * scaled to 400px wide (aspect ratio kept), immutably cached.</p>
 *
 * <p>Only classpath resources under {@code static/} or {@code public/} are
 * served — no filesystem access, no path traversal.</p>
 *
 * <p>Use with the DSL: {@code img(Img.optimized("/static/photo.jpg", 400))}
 * or plain {@code img("/jweb/img?src=/static/photo.jpg&w=400")}.</p>
 */
@Controller
public class JWebImageController {

    private static final int MAX_WIDTH = 3840;
    private static final int MAX_CACHE_ENTRIES = 500;

    private static final CacheControl IMMUTABLE = CacheControl
            .maxAge(365, TimeUnit.DAYS)
            .cachePublic()
            .immutable();

    private final Map<String, byte[]> cache = new ConcurrentHashMap<>();

    @GetMapping("/jweb/img")
    @ResponseBody
    public ResponseEntity<byte[]> optimized(@RequestParam("src") String src,
                                            @RequestParam(value = "w", required = false) Integer width) {
        String path = sanitize(src);
        if (path == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        int targetWidth = width == null ? 0 : Math.min(Math.max(width, 1), MAX_WIDTH);

        String key = path + "|" + targetWidth;
        byte[] cached = cache.get(key);
        if (cached != null) {
            return respond(cached, path);
        }

        try {
            ClassPathResource resource = new ClassPathResource(path);
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            byte[] bytes;
            try (InputStream in = resource.getInputStream()) {
                BufferedImage image = ImageIO.read(in);
                if (image == null) {
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).build();
                }
                if (targetWidth > 0 && targetWidth < image.getWidth()) {
                    image = scale(image, targetWidth);
                }
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(image, formatOf(path), out);
                bytes = out.toByteArray();
            }
            if (cache.size() < MAX_CACHE_ENTRIES) {
                cache.put(key, bytes);
            }
            return respond(bytes, path);
        } catch (Exception e) {
            com.osmig.Jweb.framework.util.Log.warn("Image optimization failed for {}: {}", path, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /** Restricts to classpath static/public resources; blocks traversal. */
    private static String sanitize(String src) {
        if (src == null || src.contains("..") || src.contains("\\")) return null;
        String path = src.startsWith("/") ? src.substring(1) : src;
        if (!path.startsWith("static/") && !path.startsWith("public/")) {
            path = "static/" + path;
        }
        return path;
    }

    private static BufferedImage scale(BufferedImage source, int targetWidth) {
        int targetHeight = (int) Math.round(
            source.getHeight() * (targetWidth / (double) source.getWidth()));
        BufferedImage scaled = new BufferedImage(targetWidth, Math.max(targetHeight, 1),
            source.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.drawImage(source, 0, 0, targetWidth, Math.max(targetHeight, 1), null);
        g.dispose();
        return scaled;
    }

    private static String formatOf(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".png")) return "png";
        if (lower.endsWith(".gif")) return "gif";
        return "jpg";
    }

    private ResponseEntity<byte[]> respond(byte[] bytes, String path) {
        MediaType type = path.toLowerCase().endsWith(".png") ? MediaType.IMAGE_PNG
            : path.toLowerCase().endsWith(".gif") ? MediaType.IMAGE_GIF
            : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().cacheControl(IMMUTABLE).contentType(type).body(bytes);
    }
}
