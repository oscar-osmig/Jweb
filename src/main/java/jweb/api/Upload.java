package jweb.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A multipart file field of a {@code @REST} method, as a {@code jweb.UploadedFile}
 * (or {@code List<UploadedFile>} for a multi-file field):
 *
 * <pre>{@code
 * @POST("/convert")
 * public Map<String, Object> convert(@Upload("file") UploadedFile file) { ... }
 * }</pre>
 *
 * <p>A missing field yields an empty {@code UploadedFile} ({@code isEmpty()}),
 * never null; a non-multipart request is a 400.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Upload {
    /** The form field name; defaults to the parameter's name. */
    String value() default "";
}
