package jweb;

/**
 * A request parameter could not be bound to a record component — missing,
 * malformed, or outside its declared range. The framework answers it with a
 * 400 and the message; catch it yourself when you call {@link Request#bind}
 * and want to render the form again instead.
 */
public class BindException extends RuntimeException {

    private final String field;

    public BindException(String field, String message) {
        super(message);
        this.field = field;
    }

    /** The record component (parameter name) that failed. */
    public String field() {
        return field;
    }
}
