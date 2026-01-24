package com.osmig.Jweb.app.docs.sections.styling;

import com.osmig.Jweb.framework.core.Element;
import static com.osmig.Jweb.app.docs.DocComponents.*;

public final class StylingAnimations {
    private StylingAnimations() {}

    public static Element render() {
        return section(
            h3Title("Keyframe Animations"),
            para("Create custom CSS animations."),

            codeBlock("""
import static com.osmig.Jweb.framework.styles.CSSAnimations.*;

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
.animation(animName("fadeIn"), s(1), ease)
//          name, duration, timing

// With all options
.animationName(animName("bounce"))
.animationDuration(s(2))
.animationTimingFunction(easeInOut)
.animationDelay(ms(500))
.animationIterationCount(iterationInfinite)
.animationDirection(directionAlternate)
.animationFillMode(fillModeForwards)

// Shorthand
.animation("fadeIn 0.5s ease-out forwards")"""),

            h3Title("Pre-built Animations"),
            para("40+ ready-to-use animations."),
            codeBlock("""
// Fade animations
fadeIn(s(0.5))
fadeOut(s(0.3))
fadeInUp(s(0.6))
fadeInDown(s(0.6))
fadeInLeft(s(0.6))
fadeInRight(s(0.6))

// Slide animations
slideInLeft(s(0.5))
slideInRight(s(0.5))
slideInUp(s(0.5))
slideInDown(s(0.5))
slideOutLeft(s(0.3))
slideOutRight(s(0.3))

// Scale animations
zoomIn(s(0.5))
zoomOut(s(0.5))
scaleIn(s(0.5))
pulse(s(1.5))
heartbeat(s(1))

// Bounce animations
bounce(s(1))
bounceIn(s(0.8))
bounceOut(s(0.8))

// Rotate animations
rotate360(s(2))
rotateIn(s(1))
flipX(s(1))
flipY(s(1))

// Attention seekers
shake(s(0.5))
wobble(s(1))
jello(s(1))
swing(s(1))
rubberBand(s(1))
flash(s(0.5))
tada(s(1))"""),

            h3Title("Animation Usage"),
            codeBlock("""
// Style with pre-built animation
style()
    .animation(fadeIn(s(0.5)))

// With modifiers
style()
    .animation(
        slideInUp(s(0.6))
            .delay(ms(200))
            .timing(easeOut)
    )

// Infinite animation
style()
    .animation(
        pulse(s(1.5))
            .iterations(infinite)
    )

// Chained animation
style()
    .animation(
        fadeInUp(s(0.5))
            .then(bounce(s(0.3)))  // After first completes
    )"""),

            h3Title("Timing Functions"),
            codeBlock("""
// Standard easing
timingEase           // Default easing
timingLinear         // Constant speed
timingEaseIn         // Slow start
timingEaseOut        // Slow end
timingEaseInOut      // Slow start and end

// Custom bezier curve
timingCubicBezier(0.4, 0, 0.2, 1)

// Steps (frame-by-frame)
timingSteps(5, "end")
timingStepStart
timingStepEnd"""),

            h3Title("Animation State"),
            codeBlock("""
// Pause/play
.animationPlayState(playStatePaused)
.animationPlayState(playStateRunning)

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
