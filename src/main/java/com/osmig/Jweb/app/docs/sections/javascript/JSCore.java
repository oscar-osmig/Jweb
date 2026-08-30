package com.osmig.Jweb.app.docs.sections.javascript;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class JSCore {
    private JSCore() {}

    public static Element render() {
        return section(
            h3Title("Core JS DSL"),
            para("Fundamental JavaScript generation with type-safe methods."),

            h3Title("DOM Access"),
            codeBlock("""
// Get element by ID
byId("myId")           // document.getElementById('myId')
$("myId")                 // Shorthand

// Query selectors
query(".my-class")        // document.querySelector('.my-class')
queryAll(".items")        // document.querySelectorAll('.items')

// Element manipulation
byId("btn")
    .addClass("active")
    .removeClass("disabled")
    .setAttribute("data-id", "123")
    .setStyle("color", "red")

// Content
byId("output").setText(v("result"))
byId("container").setHtml(v("template"))

// Visibility
byId("modal").show()
byId("tooltip").hide()"""),

            h3Title("Control Flow"),
            codeBlock("""
// If/else
func("checkAge", "age")
    .if_(v("age").gte(18))
        .then_(return_(str("adult")))
    .else_(return_(str("minor")))

// For loop
func("sum", "items")
    .let_("total", 0)
    .forOf("item", v("items"))
        .body(v("total").addAssign(v("item")))
    .endFor()
    .return_(v("total"))

// While loop
func("countdown", "n")
    .while_(v("n").gt(0))
        .body("n--", call("console.log", v("n")))
    .endWhile()

// Switch
func("handleAction", "action")
    .switch_(v("action"))
        .case_("add").then_(call("add"), "break")
        .case_("remove").then_(call("remove"), "break")
        .default_().then_(call("noop"))
    .endSwitch()"""),

            h3Title("Array Methods"),
            para("30+ fluent array operations."),
            codeBlock("""
// Filter and map
v("items")
    .filter(callback("x").return_(v("x").gt(5)))
    .map(callback("x").return_(v("x").times(2)))
    .join(", ")

// Find
v("users")
    .find(callback("u").return_(v("u").dot("id").eq(userId)))

// Reduce
v("numbers").reduce(
    callback("acc", "n").return_(v("acc").plus(v("n"))),
    0
)

// Other methods
.forEach(callback)    // Iterate
.some(predicate)      // Any match?
.every(predicate)     // All match?
.includes(value)      // Contains?
.indexOf(value)       // Find index
.slice(start, end)    // Get slice
.concat(other)        // Combine
.reverse()            // Reverse
.sort(comparator)     // Sort
.flat()               // Flatten
.first()              // First element
.last()               // Last element"""),

            h3Title("String Methods"),
            para("20+ string operations."),
            codeBlock("""
v("text")
    .substring(0, 10)
    .toLowerCase()
    .trim()

// Search
.indexOf("search")
.includes("sub")
.startsWith("prefix")
.endsWith("suffix")

// Transform
.replace("old", "new")
.replaceAll("pattern", "replacement")
.split(",")
.repeat(3)
.padStart(5, "0")
.padEnd(10, " ")"""),

            h3Title("Object Methods"),
            codeBlock("""
// Get properties
v("obj").keys()      // Object.keys(obj)
v("obj").values()    // Object.values(obj)
v("obj").entries()   // Object.entries(obj)

// Check property
v("obj").hasOwnProperty("key")

// Static methods
objectAssign(v("target"), v("source"))
objectFreeze(v("config"))
objectFromEntries(v("entries"))"""),

            h3Title("Operators"),
            codeBlock("""
// Comparison
v("a").eq(v("b"))    // a === b
v("a").neq(v("b"))   // a !== b
v("a").gt(5)                // a > 5
v("a").gte(5)               // a >= 5
v("a").lt(10)               // a < 10
v("a").lte(10)              // a <= 10

// Logical
v("a").and(v("b"))   // a && b
v("a").or(v("b"))    // a || b
v("flag").not()             // !flag

// Arithmetic
v("a").plus(v("b"))  // a + b
v("a").minus(5)             // a - 5
v("a").times(2)             // a * 2
v("a").div(10)              // a / 10
v("a").mod(2)               // a % 2

// Optional chaining
optionalChain(v("user"), "address", "city")  // user?.address?.city

// Nullish coalescing
nullishCoalesce(v("name"), str("Anonymous"))  // name ?? 'Anonymous'""")
        );
    }
}
