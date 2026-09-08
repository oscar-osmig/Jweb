package com.osmig.Jweb.framework.api;

import com.osmig.Jweb.framework.routing.ParamConvert;
import com.osmig.Jweb.framework.util.Json;
import jakarta.servlet.http.HttpServletRequest;
import jweb.UploadedFile;
import jweb.api.Body;
import jweb.api.Cookie;
import jweb.api.Header;
import jweb.api.Param;
import jweb.api.Query;
import jweb.api.Upload;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.HandlerMapping;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the {@code jweb.api} parameter annotations — {@code @Body},
 * {@code @Param}, {@code @Query}, {@code @Header}, {@code @Cookie},
 * {@code @Upload} — and a bare {@code jweb.Request} parameter in
 * {@code @REST} methods. Spring's own parameter annotations target
 * {@code PARAMETER} only, so they cannot be meta-annotated the way
 * {@code @GET} wraps {@code @RequestMapping}; this resolver is the
 * equivalent, registered by {@link JWebApiMvcConfig}.
 *
 * <p>Conversion follows {@link ParamConvert} (numbers, booleans, UUID,
 * enums by name); {@code Optional<T>} and {@code List<T>} wrap it. Missing
 * or malformed input is a 400 with a message naming the parameter.</p>
 */
public final class JWebArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType() == jweb.Request.class
            || parameter.hasParameterAnnotation(Body.class)
            || parameter.hasParameterAnnotation(Param.class)
            || parameter.hasParameterAnnotation(Query.class)
            || parameter.hasParameterAnnotation(Header.class)
            || parameter.hasParameterAnnotation(Cookie.class)
            || parameter.hasParameterAnnotation(Upload.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servlet = webRequest.getNativeRequest(HttpServletRequest.class);

        if (parameter.getParameterType() == jweb.Request.class) {
            jweb.Request request = new jweb.Request(servlet);
            request.setPathParams(pathVariables(webRequest));
            return request;
        }

        Body body = parameter.getParameterAnnotation(Body.class);
        if (body != null) return resolveBody(parameter, servlet, body.required());

        Param param = parameter.getParameterAnnotation(Param.class);
        if (param != null) {
            String name = nameOf(param.value(), parameter);
            String raw = pathVariables(webRequest).get(name);
            if (raw == null) throw badRequest("Path parameter '" + name + "' is missing");
            return convert(parameter, name, raw);
        }

        Query query = parameter.getParameterAnnotation(Query.class);
        if (query != null) {
            String name = nameOf(query.value(), parameter);
            String[] values = servlet.getParameterValues(name);
            return resolveValues(parameter, name, values, query.defaultValue(), query.required(), "Query parameter");
        }

        Header header = parameter.getParameterAnnotation(Header.class);
        if (header != null) {
            String name = nameOf(header.value(), parameter);
            List<String> values = java.util.Collections.list(servlet.getHeaders(name));
            return resolveValues(parameter, name, values.isEmpty() ? null : values.toArray(String[]::new),
                header.defaultValue(), header.required(), "Header");
        }

        Cookie cookie = parameter.getParameterAnnotation(Cookie.class);
        if (cookie != null) {
            String name = nameOf(cookie.value(), parameter);
            String value = null;
            if (servlet.getCookies() != null) {
                for (jakarta.servlet.http.Cookie c : servlet.getCookies()) {
                    if (name.equals(c.getName())) { value = c.getValue(); break; }
                }
            }
            return resolveValues(parameter, name, value == null ? null : new String[]{value},
                cookie.defaultValue(), cookie.required(), "Cookie");
        }

        Upload upload = parameter.getParameterAnnotation(Upload.class);
        if (upload != null) return resolveUpload(parameter, servlet, nameOf(upload.value(), parameter));

        throw new IllegalStateException("Unsupported parameter: " + parameter);
    }

    // ==================== Body ====================

    private Object resolveBody(MethodParameter parameter, HttpServletRequest servlet, boolean required)
            throws java.io.IOException {
        String text = new String(servlet.getInputStream().readAllBytes(),
            servlet.getCharacterEncoding() != null ? servlet.getCharacterEncoding() : "UTF-8");
        Class<?> type = parameter.getParameterType();
        if (text.isBlank()) {
            if (required) throw badRequest("Request body is required");
            return type == String.class ? "" : type == Optional.class ? Optional.empty() : null;
        }
        if (type == String.class) return text;
        try {
            Type target = type == Optional.class ? typeArgument(parameter.getGenericParameterType()) : parameter.getGenericParameterType();
            Object value = Json.mapper().readValue(text, Json.mapper().constructType(target));
            return type == Optional.class ? Optional.ofNullable(value) : value;
        } catch (java.io.IOException | RuntimeException e) {
            throw badRequest("Request body is not valid JSON for " + type.getSimpleName() + ": " + rootMessage(e));
        }
    }

    // ==================== Simple values ====================

    private Object resolveValues(MethodParameter parameter, String name, String[] values,
                                 String defaultValue, boolean required, String what) {
        Class<?> type = parameter.getParameterType();
        boolean present = values != null && values.length > 0 && values[0] != null && !values[0].isEmpty();

        if (type == List.class) {
            Class<?> element = typeArgumentClass(parameter.getGenericParameterType());
            List<Object> out = new ArrayList<>();
            if (values != null) {
                for (String raw : values) {
                    if (raw == null || raw.isEmpty()) continue;
                    out.add(convertTo(element, name, raw));
                }
            }
            if (out.isEmpty() && !defaultValue.isEmpty()) out.add(convertTo(element, name, defaultValue));
            return List.copyOf(out);
        }
        if (type == Optional.class) {
            Class<?> inner = typeArgumentClass(parameter.getGenericParameterType());
            if (!present) return defaultValue.isEmpty() ? Optional.empty() : Optional.of(convertTo(inner, name, defaultValue));
            return Optional.of(convertTo(inner, name, values[0]));
        }
        if (!present) {
            if (!defaultValue.isEmpty()) return convertTo(type, name, defaultValue);
            if (type == boolean.class) return Boolean.FALSE;
            if (required) throw badRequest(what + " '" + name + "' is required");
            return type.isPrimitive() ? primitiveZero(type) : null;
        }
        return convert(parameter, name, values[0]);
    }

    private Object convert(MethodParameter parameter, String name, String raw) {
        Class<?> type = parameter.getParameterType();
        if (type == Optional.class) {
            return Optional.of(convertTo(typeArgumentClass(parameter.getGenericParameterType()), name, raw));
        }
        return convertTo(type, name, raw);
    }

    private static Object convertTo(Class<?> type, String name, String raw) {
        try {
            return ParamConvert.convert(raw, type);
        } catch (IllegalArgumentException e) {
            throw badRequest("Parameter '" + name + "' " + e.getMessage());
        }
    }

    private static Object primitiveZero(Class<?> type) {
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0d;
        if (type == float.class) return 0f;
        if (type == short.class) return (short) 0;
        if (type == byte.class) return (byte) 0;
        if (type == char.class) return '\0';
        return false;
    }

    // ==================== Uploads ====================

    private Object resolveUpload(MethodParameter parameter, HttpServletRequest servlet, String name) {
        MultipartHttpServletRequest multipart = servlet instanceof MultipartHttpServletRequest m ? m : null;
        if (multipart == null) {
            throw badRequest("Upload '" + name + "' needs a multipart/form-data request");
        }
        if (parameter.getParameterType() == List.class) {
            List<UploadedFile> files = new ArrayList<>();
            for (MultipartFile file : multipart.getFiles(name)) files.add(new UploadedFile(file));
            return List.copyOf(files);
        }
        MultipartFile file = multipart.getFile(name);
        return new UploadedFile(file != null ? file : new EmptyMultipartFile(name));
    }

    /** What a missing multipart field yields: an empty file, never null. */
    private record EmptyMultipartFile(String field) implements MultipartFile {
        @Override public String getName() { return field; }
        @Override public String getOriginalFilename() { return ""; }
        @Override public String getContentType() { return null; }
        @Override public boolean isEmpty() { return true; }
        @Override public long getSize() { return 0; }
        @Override public byte[] getBytes() { return new byte[0]; }
        @Override public java.io.InputStream getInputStream() { return java.io.InputStream.nullInputStream(); }
        @Override public void transferTo(java.io.File dest) throws java.io.IOException {
            throw new java.io.IOException("No file was uploaded for '" + field + "'");
        }
    }

    // ==================== Helpers ====================

    @SuppressWarnings("unchecked")
    private static Map<String, String> pathVariables(NativeWebRequest webRequest) {
        Object vars = webRequest.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST);
        return vars instanceof Map<?, ?> map ? (Map<String, String>) map : Map.of();
    }

    private static String nameOf(String declared, MethodParameter parameter) {
        if (!declared.isEmpty()) return declared;
        String name = parameter.getParameterName();
        if (name == null) {
            throw new IllegalStateException("Cannot infer the name of parameter " + parameter.getParameterIndex()
                + " of " + parameter.getMethod() + " — compile with -parameters or name it in the annotation");
        }
        return name;
    }

    private static Type typeArgument(Type generic) {
        if (generic instanceof ParameterizedType p && p.getActualTypeArguments().length == 1) {
            return p.getActualTypeArguments()[0];
        }
        throw new IllegalStateException("Declare the element type: " + generic.getTypeName());
    }

    private static Class<?> typeArgumentClass(Type generic) {
        Type arg = typeArgument(generic);
        if (arg instanceof Class<?> c) return c;
        throw new IllegalStateException("Unsupported element type: " + arg.getTypeName());
    }

    private static String rootMessage(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        String m = t.getMessage();
        return m == null ? t.getClass().getSimpleName() : m.lines().findFirst().orElse(m);
    }

    private static ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
