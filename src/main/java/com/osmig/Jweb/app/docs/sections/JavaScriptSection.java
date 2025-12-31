package com.osmig.Jweb.app.docs.sections;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.app.docs.sections.javascript.*;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JavaScriptSection {
    private JavaScriptSection() {}

    public static Element render() {
        return section(
            docTitle("JavaScript DSL"),
            para("JWeb provides two JavaScript DSLs: a low-level JS DSL for general scripting " +
                 "and a high-level Actions DSL for common UI interactions."),

            docSubtitle("JS DSL"),
            para("The JS class provides a fluent API for generating JavaScript code from Java."),
            codeBlock("""
                import static com.osmig.Jweb.framework.js.JS.*;

                // Create a script with variables and functions
                String js = script()
                    .var_("count", 0)
                    .add(func("increment")
                        .set("count", variable("count").plus(1))
                        .call("updateDisplay"))
                    .build();"""),

            h3Title("Variables and Values"),
            para("Declare variables and create values with type-safe methods."),
            codeBlock("""
                script()
                    .var_("name", "John")     // var name='John'
                    .let_("age", 25)          // let age=25
                    .const_("PI", 3.14159)    // const PI=3.14159
                    .build()

                // Value references
                variable("count")             // count
                str("hello")                  // 'hello'
                array(1, 2, 3)               // [1,2,3]
                obj("name", "John", "age", 30) // {name:'John',age:30}"""),

            h3Title("Functions"),
            para("Create named functions and callbacks with parameters."),
            codeBlock("""
                // Named function
                func("greet", "name")
                    .var_("msg", str("Hello, ").plus(variable("name")))
                    .call("console.log", variable("msg"))
                    .ret(variable("msg"))

                // Callback (anonymous function)
                callback("item")
                    .ret(variable("item").times(2))"""),

            h3Title("Control Flow"),
            para("Conditionals, loops, and switch statements."),
            codeBlock("""
                // If/else
                func("checkAge", "age")
                    .if_(variable("age").gte(18))
                        .then_(ret("adult"))
                    .else_(ret("minor"))

                // For loop
                func("sum", "items")
                    .let_("total", 0)
                    .forOf("item", variable("items"))
                        .body(variable("total").addAssign(variable("item")))
                    .endFor()
                    .ret(variable("total"))

                // While loop
                func("countdown", "n")
                    .while_(variable("n").gt(0))
                        .body("n--", call("console.log", variable("n")))
                    .endWhile()

                // Switch
                func("handleAction", "action")
                    .switch_(variable("action"))
                        .case_("add").then_(call("add"), "break")
                        .case_("remove").then_(call("remove"), "break")
                        .default_().then_(call("noop"))
                    .endSwitch()"""),

            h3Title("DOM Manipulation"),
            para("Type-safe DOM element methods."),
            codeBlock("""
                // Get elements
                getElem("myId")               // document.getElementById('myId')
                query(".my-class")            // document.querySelector('.my-class')

                // Manipulate elements
                getElem("btn")
                    .addClass("active")
                    .removeClass("disabled")
                    .setAttribute("data-id", "123")
                    .setStyle("color", "red")

                // Content
                getElem("output").setText(variable("result"))
                getElem("container").setHtml(variable("template"))

                // Visibility
                getElem("modal").show()
                getElem("tooltip").hide()"""),

            h3Title("Array Methods"),
            para("Fluent array operations."),
            codeBlock("""
                variable("items")
                    .filter(callback("x").ret(variable("x").gt(5)))
                    .map(callback("x").ret(variable("x").times(2)))
                    .join(", ")

                variable("users")
                    .find(callback("u").ret(variable("u").dot("id").eq(userId)))

                variable("numbers").reduce(
                    callback("acc", "n").ret(variable("acc").plus(variable("n"))),
                    0
                )"""),

            docSubtitle("Actions DSL"),
            para("The Actions DSL provides high-level builders for common UI patterns."),
            codeBlock("""
                import static com.osmig.Jweb.framework.js.Actions.*;

                // Build a complete script with refs, functions, and lifecycle
                actions()
                    .refs(refs()
                        .add("btn", "submit-btn")
                        .add("output", "result-display"))
                    .add(func("handleSubmit")
                        .fetch("/api/submit")
                        .method("POST")
                        .body(formData("myForm"))
                        .onSuccess(setHtml("output", response("message")))
                        .build())
                    .onReady(call("init"))
                    .build()"""),

            h3Title("Fetch Builder"),
            para("Type-safe fetch requests with response handling."),
            codeBlock("""
                fetch("/api/users")
                    .method("GET")
                    .onSuccess(setHtml("userList", response("html")))
                    .onError(alertModal("modal").error("Failed to load users"))
                    .build()

                fetch("/api/login")
                    .method("POST")
                    .body(formData("loginForm"))
                    .onSuccess(
                        actions()
                            .add(setHtml("status", str("Welcome!")))
                            .add(redirect("/dashboard"))
                    )
                    .build()"""),

            h3Title("UI Actions"),
            para("Common UI interactions."),
            codeBlock("""
                // Modals
                alertModal("modal-overlay")
                    .success("Operation completed!")

                confirmDialog("modal-overlay")
                    .message("Delete this item?")
                    .danger()
                    .onConfirm(fetch("/api/delete/" + id).method("DELETE").build())
                    .onCancel(log("Cancelled"))

                // Element manipulation
                addClass("element-id", "active")
                removeClass("element-id", "hidden")
                toggleClass("element-id", "expanded")
                setHtml("container", variable("content"))
                setText("label", str("Updated"))

                // Navigation
                redirect("/new-page")
                reload()"""),

            h3Title("Templates"),
            para("Build HTML templates for dynamic rendering."),
            codeBlock("""
                // Template for rendering list items
                template("item")
                    .div().class_("card")
                        .div().style("font-weight:600")
                            .text(escapedField("name"))
                        .end()
                        .div().style("color:#666")
                            .text(escapedField("email"))
                        .end()
                        .statusBadge("status", "statusBg", "statusTxt")
                    .end()
                    .buildAs("renderCard")

                // Color switch for status badges
                colorSwitch("status")
                    .when("active", "#22c55e")
                    .when("pending", "#f59e0b")
                    .when("inactive", "#ef4444")
                    .defaultColor("#6b7280")
                    .buildAs("statusBg")"""),

            h3Title("Event Handlers"),
            para("Attach event handlers to elements."),
            codeBlock("""
                // Window functions (globally accessible)
                windowFunc("handleClick")
                    .params("event")
                    .async()
                    .add(fetch("/api/action").method("POST").build())
                    .build()

                // In HTML: onclick="handleClick(event)"

                // Lifecycle events
                actions()
                    .onReady(call("initialize"))
                    .onLoad(call("loadData"))
                    .build()"""),

            docTip("The JS DSL generates minified JavaScript. Use browser DevTools to inspect generated code."),

            JSAsync.render(),
            JSFetchAdvanced.render(),
            JSDomQuery.render(),

            spacer()
        );
    }
}
