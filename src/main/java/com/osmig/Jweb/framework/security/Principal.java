package com.osmig.Jweb.framework.security;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Represents an authenticated user/principal in the system.
 *
 * <p>This is a simple, framework-agnostic representation of the
 * currently authenticated user. It can be extended or wrapped
 * to integrate with specific authentication systems.</p>
 *
 * <p>Usage:</p>
 * <pre>
 * // In a route handler
 * Principal user = Auth.getPrincipal(request);
 * if (user != null) {
 *     String userId = user.getId();
 *     String username = user.getName();
 *     boolean isAdmin = user.hasRole("admin");
 * }
 * </pre>
 */
public class Principal {

    private final String id;
    private final String name;
    private final Set<String> roles;
    private final Map<String, Object> attributes;

    private Principal(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.roles = builder.roles != null ? Set.copyOf(builder.roles) : Collections.emptySet();
        this.attributes = builder.attributes != null ? Map.copyOf(builder.attributes) : Collections.emptyMap();
    }

    /**
     * Gets the unique identifier for this principal.
     *
     * @return the principal ID
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the display name of this principal.
     *
     * @return the principal name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets all roles assigned to this principal.
     *
     * @return unmodifiable set of roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    /**
     * Checks if this principal has a specific role.
     *
     * @param role the role to check
     * @return true if the principal has the role
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if this principal has any of the specified roles.
     *
     * @param requiredRoles the roles to check
     * @return true if the principal has at least one of the roles
     */
    public boolean hasAnyRole(String... requiredRoles) {
        for (String role : requiredRoles) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this principal has all of the specified roles.
     *
     * @param requiredRoles the roles to check
     * @return true if the principal has all of the roles
     */
    public boolean hasAllRoles(String... requiredRoles) {
        for (String role : requiredRoles) {
            if (!roles.contains(role)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets all custom attributes for this principal.
     *
     * @return unmodifiable map of attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Gets a custom attribute value.
     *
     * @param key the attribute key
     * @param <T> the expected type
     * @return the attribute value, or null if not present
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key) {
        return (T) attributes.get(key);
    }

    /**
     * Gets a custom attribute value with a default.
     *
     * @param key          the attribute key
     * @param defaultValue the default value if not present
     * @param <T>          the expected type
     * @return the attribute value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, T defaultValue) {
        Object value = attributes.get(key);
        return value != null ? (T) value : defaultValue;
    }

    @Override
    public String toString() {
        return "Principal[id=" + id + ", name=" + name + ", roles=" + roles + "]";
    }

    // ========== Builder ==========

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String id;
        private String name;
        private Set<String> roles;
        private Map<String, Object> attributes;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder roles(Set<String> roles) {
            this.roles = roles;
            return this;
        }

        public Builder roles(String... roles) {
            this.roles = Set.of(roles);
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder attribute(String key, Object value) {
            if (this.attributes == null) {
                this.attributes = new java.util.HashMap<>();
            }
            this.attributes.put(key, value);
            return this;
        }

        public Principal build() {
            if (id == null || id.isBlank()) {
                throw new IllegalStateException("Principal ID is required");
            }
            return new Principal(this);
        }
    }

    // ========== Factory Methods ==========

    /**
     * Creates a simple principal with just an ID.
     */
    public static Principal of(String id) {
        return builder().id(id).name(id).build();
    }

    /**
     * Creates a principal with ID and name.
     */
    public static Principal of(String id, String name) {
        return builder().id(id).name(name).build();
    }

    /**
     * Creates a principal with ID, name, and roles.
     */
    public static Principal of(String id, String name, String... roles) {
        return builder().id(id).name(name).roles(roles).build();
    }
}
