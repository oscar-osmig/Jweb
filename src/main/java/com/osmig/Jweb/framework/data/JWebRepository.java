package com.osmig.Jweb.framework.data;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

/**
 * Base repository interface for JWeb applications.
 *
 * <p>Extends JpaRepository with additional convenience methods
 * and specification support for dynamic queries.</p>
 *
 * <p>This interface is optional - you can use standard JpaRepository
 * or CrudRepository instead.</p>
 *
 * <h2>Example</h2>
 * <pre>{@code
 * public interface UserRepository extends JWebRepository<User, Long> {
 *     // All JpaRepository methods are available
 *     // Plus JpaSpecificationExecutor for dynamic queries
 *
 *     Optional<User> findByEmail(String email);
 * }
 * }</pre>
 *
 * <h2>Convenience Methods</h2>
 * <ul>
 *   <li>{@link #getOrThrow(Object)} - Find by ID, throw if not found</li>
 *   <li>{@link #hasAny()} - Check if any entities exist</li>
 *   <li>{@link #findFirst()} - Get the first entity</li>
 * </ul>
 *
 * @param <T> the entity type
 * @param <ID> the ID type
 */
@NoRepositoryBean
public interface JWebRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find entity by ID, throwing if not found.
     *
     * @param id the entity ID
     * @return the entity
     * @throws EntityNotFoundException if not found
     */
    default T getOrThrow(ID id) {
        return findById(id).orElseThrow(() ->
            new EntityNotFoundException("Entity not found with id: " + id)
        );
    }

    /**
     * Check if any entities exist.
     *
     * @return true if at least one entity exists
     */
    default boolean hasAny() {
        return count() > 0;
    }

    /**
     * Find the first entity (useful for singletons or settings).
     *
     * @return the first entity, or empty
     */
    default Optional<T> findFirst() {
        List<T> all = findAll();
        return all.isEmpty() ? Optional.empty() : Optional.of(all.getFirst());
    }
}
