package com.osmig.Jweb.app.docs.sections.state;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateObjects {
    private StateObjects() {}

    public static Element render() {
        return section(
            h3Title("State with Objects"),
            para("Store complex objects in state."),
            codeBlock("""
// User record or class
record User(String name, String email, boolean admin) {
    User withName(String name) {
        return new User(name, email, admin);
    }
    User withEmail(String email) {
        return new User(name, email, admin);
    }
}

State<User> user = useState(new User("John", "john@test.com", false));

// Update object
user.update(u -> u.withName("Jane"));
user.update(u -> u.withEmail("jane@test.com"));

// Display
div(
    p("Name: " + user.get().name()),
    p("Email: " + user.get().email()),
    when(user.get().admin(), () -> span("Admin"))
)"""),

            h3Title("Nested Object Updates"),
            codeBlock("""
record Address(String street, String city) {}
record Person(String name, Address address) {}

State<Person> person = useState(
    new Person("John", new Address("123 Main", "NYC"))
);

// Update nested property - create new objects
person.update(p -> new Person(
    p.name(),
    new Address("456 Oak", p.address().city())
));""")
        );
    }
}
