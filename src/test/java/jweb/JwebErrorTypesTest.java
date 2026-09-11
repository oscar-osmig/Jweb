package jweb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The exception types an author catches are real {@code jweb.*} classes (rule of 2026-09-07). */
class JwebErrorTypesTest {

    @Test
    void theFrameworkExceptionsAreJwebTypes() {
        JWebException notFound = JWebException.notFound("page");
        assertEquals(404, notFound.getStatus().value());

        JWebException byNumber = new JWebException(418, "teapot");
        assertEquals(418, byNumber.getStatus().value());

        ValidationException invalid = ValidationException.of("email", "required");
        assertTrue(invalid.getValidationResult().hasErrors());
        assertInstanceOf(JWebException.class, invalid);
    }

    @Test
    @SuppressWarnings("deprecation")
    void theOldNamesStillCompileAndAreCaughtByTheNewOnes() {
        try {
            throw new com.osmig.Jweb.framework.error.ValidationException(ValidationException.of("f", "m").getValidationResult());
        } catch (ValidationException e) {
            assertNotNull(e.getValidationResult());
        }
    }
}
