package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingEffects {
    private StylingEffects() {}

    public static Element render() {
        return section(
            h3Title("Visual Effects"),
            para("Shadows, filters, transforms, and transitions."),

            h3Title("Box Shadow"),
            codeBlock("""
// Basic shadow
.boxShadow(px(0), px(4), px(6), rgba(0, 0, 0, 0.1))
//         x-offset, y-offset, blur, color

// With spread
.boxShadow(px(0), px(4), px(6), px(2), rgba(0, 0, 0, 0.1))
//         x, y, blur, spread, color

// String syntax
.boxShadow("0 4px 6px rgba(0,0,0,0.1)")

// Presets
.shadowXs()         // Subtle shadow
.shadowSm()         // Small shadow
.shadow()           // Default shadow
.shadowMd()         // Medium shadow
.shadowLg()         // Large shadow
.shadowXl()         // Extra large shadow
.shadowInner()      // Inset shadow
.shadowNone()       // No shadow"""),

            h3Title("Transforms"),
            para("2D and 3D transformations."),
            codeBlock("""
// Translate (move)
.transform(translateX(px(10)))
.transform(translateY(px(20)))
.transform(translate(px(10), px(20)))

// Rotate
.transform(rotate(deg(45)))
.transform(rotate(turn(0.25)))  // Quarter turn

// Scale
.transform(scale(1.2))          // Uniform
.transform(scale(2, 0.5))       // Non-uniform
.transform(scaleX(1.5))
.transform(scaleY(0.8))

// Skew
.transform(skewX(deg(10)))
.transform(skewY(deg(10)))

// Multiple transforms
.transform(translateY(px(-4)), scale(1.05))

// Transform origin
.transformOrigin("center center")
.transformOrigin("top left")"""),

            h3Title("Transitions"),
            para("Animate property changes."),
            codeBlock("""
// Simple transition
.transition(propAll, s(0.3), ease)
//          property, duration, timing

// Specific property
.transition(propColor, s(0.2))
.transition(propBackground, s(0.3), easeInOut)
.transition(propTransform, s(0.2), easeOut)

// Multiple properties
.transition("color 0.2s, background 0.3s, transform 0.2s")

// Individual settings
.transitionProperty(propAll)
.transitionDuration(s(0.3))
.transitionTimingFunction(easeInOut)
.transitionDelay(ms(100))

// Timing functions
ease, linear, easeIn, easeOut, easeInOut

// Custom bezier
.transitionTimingFunction(cubicBezier(0.4, 0, 0.2, 1))"""),

            h3Title("Filters"),
            para("Image and element filters."),
            codeBlock("""
// Blur
.filter(blur(px(5)))

// Brightness (1 = normal)
.filter(brightness(1.2))

// Contrast
.filter(contrast(1.5))

// Grayscale
.filter(grayscale(percent(100)))

// Opacity filter
.filter(opacity(percent(50)))

// Saturate
.filter(saturate(percent(200)))

// Sepia
.filter(sepia(percent(100)))

// Drop shadow (for transparent images)
.filter(dropShadow(px(2), px(4), px(6), rgba(0, 0, 0, 0.3)))

// Multiple filters
.filter("blur(5px) brightness(1.1)")"""),

            h3Title("Backdrop Filter"),
            para("Filter the area behind an element (for glass effects)."),
            codeBlock("""
// Frosted glass effect
.backgroundColor(rgba(255, 255, 255, 0.7))
.backdropFilter(blur(px(10)))

// Glass card
style()
    .backgroundColor(rgba(255, 255, 255, 0.2))
    .backdropFilter(blur(px(20)))
    .borderRadius(px(16))
    .border(px(1), solid, rgba(255, 255, 255, 0.3))"""),

            h3Title("Opacity"),
            codeBlock("""
.opacity(1)         // Fully visible
.opacity(0.5)       // Half transparent
.opacity(0)         // Invisible"""),

            h3Title("Cursor & Interaction"),
            codeBlock("""
// Cursor styles
.cursor(pointer)    // Hand cursor (clickable)
.cursor(notAllowed) // Disabled cursor
.cursor(grab)       // Grab cursor
.cursor(text)       // Text selection cursor

// Disable interactions
.pointerEvents(none)  // Click-through
.userSelect(none)     // No text selection""")
        );
    }
}
