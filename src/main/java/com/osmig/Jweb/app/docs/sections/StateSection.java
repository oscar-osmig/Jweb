package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.state.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StateSection {
    private StateSection() {}

    public static Element render() {
        return section(
            docTitle("State Management"),
            para("JWeb provides reactive state management inspired by React hooks. " +
                 "State changes automatically trigger UI updates via WebSocket."),

            docSubtitle("Overview"),
            para("Use useState() to create reactive state variables. " +
                 "When state changes, the component re-renders automatically."),
            codeBlock("""
                    import static com.osmig.Jweb.framework.state.StateHooks.*;
                    
                    // Create state with initial value
                    State<Integer> count = useState(0);
                    
                    // Read: count.get()
                    // Write: count.set(newValue)
                    // Update: count.update(current -> newValue)"""),

            StateBasics.render(),
            StateUpdates.render(),
            StateObjects.render(),
            StateLists.render(),
            StateAdvanced.render()
        );
    }
}
