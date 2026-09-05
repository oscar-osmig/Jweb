package com.osmig.Jweb.app.docs.sections.styling;

import jweb.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingAnimations {
    private StylingAnimations() {}

    public static Element render() {
        return section(
            h3Title("Keyframe Animations"),
            para("Create custom CSS animations."),

            codeBlock("""
import static jweb.Css.*;

// Define keyframes
String fadeInKeyframes = keyframes("fadeIn")
    .from(style().opacity(0))
    .to(style().opacity(1))
    .build();

// Multi-step animation
String bounceKeyframes = keyframes("bounce")
    .at(0, style().transform(translateY(zero)))
    .at(50, style().transform(translateY(px(-20))))
    .at(100, style().transform(translateY(zero)))
    .build();"""),

            h3Title("Using Animations"),
            codeBlock("""
// Apply animation to element
.animation("fadeIn", s(1), ease)
//          name, duration, timing

// With all options
.animationName("bounce")
.animationDuration(s(2))
.animationTimingFunction(easeInOut)
.animationDelay(ms(500))
.animationIterationCount(infinite)
.animationDirection(directionAlternate)
.animationFillMode(fillModeForwards)

// Shorthand (raw CSS string)
.prop("animation", "fadeIn 0.5s ease-out forwards")"""),

            h3Title("Pre-built Animations"),
            para("Eleven ready-to-use animations. Each one has matching @keyframes in "
                 + "Keyframes, so it animates as soon as you add that stylesheet."),
            codeBlock("""
// Fade
fadeIn(s(0.5))
fadeOut(s(0.3))

// Slide
slideInLeft(s(0.5))
slideInRight(s(0.5))

// Scale
zoomIn(s(0.5))
zoomOut(s(0.5))
pulse(s(1.5))

// Bounce / rotate
bounce(s(1))
spin(s(2))

// Attention
shake(s(0.5))

// Add the matching keyframes to your stylesheet:
stylesheet().add(Keyframes.fadeIn()).add(Keyframes.spin())

// For anything else, write the keyframes and name them:
keyframes("wobble")
    .from(style().transform("rotate(-3deg)"))
    .to(style().transform("rotate(3deg)"))"""),

            h3Title("Animation Usage"),
            codeBlock("""
// Style with pre-built animation
style()
    .prop("animation", fadeIn(s(0.5)))

// With modifiers
style()
    .prop("animation",
        slideInLeft(s(0.6))
            .delay(ms(200))
            .timing(easeOut)
    )

// Infinite animation
style()
    .prop("animation",
        pulse(s(1.5))
            .iterationCount(infinite)
    )

// Multiple animations at once
style()
    .animationName(composeAnimations("fadeIn", "bounce"))
    .animationDuration(s(0.5))"""),

            h3Title("Timing Functions"),
            codeBlock("""
// Standard easing — the CSS keyword names
ease                 // Default easing
linear               // Constant speed
easeIn               // Slow start
easeOut              // Slow end
easeInOut            // Slow start and end

// Custom bezier curve
cubicBezier(0.4, 0, 0.2, 1)

// Steps (frame-by-frame)
steps(5, "end")
stepStart
stepEnd"""),

            h3Title("Animation State"),
            codeBlock("""
// Pause/play
.animationPlayState(paused)
.animationPlayState(running)

// Fill mode (final state)
.animationFillMode(fillModeForwards)  // Keep end state
.animationFillMode(fillModeBackwards) // Apply start before delay
.animationFillMode(fillModeBoth)      // Both

// Direction
.animationDirection(directionNormal)
.animationDirection(directionReverse)
.animationDirection(directionAlternate)"""),

            docTip("Use prefers-reduced-motion media query to disable animations for users who prefer reduced motion.")
        );
    }
}
