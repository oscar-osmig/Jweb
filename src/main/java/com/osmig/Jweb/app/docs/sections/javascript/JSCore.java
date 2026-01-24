package com.osmig.Jweb.app.docs.sections.javascript;

import com.osmig.Jweb.framework.core.Element;
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
getElem("myId")           // document.getElementById('myId')
$("myId")                 // Shorthand

// Query selectors
query(".my-class")        // document.querySelector('.my-class')
queryAll(".items")        // document.querySelectorAll('.items')

// Element manipulation
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

            h3Title("Control Flow"),
            codeBlock("""
// If/else
func("checkAge", "age")
    .if_(variable("age").gte(18))
        .then_(ret(str("adult")))
    .else_(ret(str("minor")))

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

            h3Title("Array Methods"),
            para("30+ fluent array operations."),
            codeBlock("""
// Filter and map
variable("items")
    .filter(callback("x").ret(variable("x").gt(5)))
    .map(callback("x").ret(variable("x").times(2)))
    .join(", ")

// Find
variable("users")
    .find(callback("u").ret(variable("u").dot("id").eq(userId)))

// Reduce
variable("numbers").reduce(
    callback("acc", "n").ret(variable("acc").plus(variable("n"))),
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
variable("text")
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
variable("obj").keys()      // Object.keys(obj)
variable("obj").values()    // Object.values(obj)
variable("obj").entries()   // Object.entries(obj)

// Check property
variable("obj").hasOwnProperty("key")

// Static methods
objectAssign(variable("target"), variable("source"))
objectFreeze(variable("config"))
objectFromEntries(variable("entries"))"""),

            h3Title("Operators"),
            codeBlock("""
// Comparison
variable("a").eq(variable("b"))    // a === b
variable("a").neq(variable("b"))   // a !== b
variable("a").gt(5)                // a > 5
variable("a").gte(5)               // a >= 5
variable("a").lt(10)               // a < 10
variable("a").lte(10)              // a <= 10

// Logical
variable("a").and(variable("b"))   // a && b
variable("a").or(variable("b"))    // a || b
not(variable("flag"))              // !flag

// Arithmetic
variable("a").plus(variable("b"))  // a + b
variable("a").minus(5)             // a - 5
variable("a").times(2)             // a * 2
variable("a").divide(10)           // a / 10
variable("a").mod(2)               // a % 2

// Optional chaining
optionalChain(variable("user"), "address", "city")  // user?.address?.city

// Nullish coalescing
nullishCoalesce(variable("name"), str("Anonymous"))  // name ?? 'Anonymous'""")
        );
    }
}
