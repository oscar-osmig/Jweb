package com.osmig.Jweb.framework.navigation;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.elements.TextElement;

import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Client-side navigation runtime for enhanced page transitions.
 *
 * <p>Navigation provides the JavaScript runtime needed for prefetching,
 * partial page swaps, and smooth transitions. Include the script in your
 * layout to enable these features.</p>
 *
 * <h2>Setup</h2>
 * <pre>{@code
 * // In your layout's head or end of body
 * Navigation.script()
 *
 * // Or include the script inline
 * inlineScript(Navigation.js())
 * }</pre>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li><b>Prefetch</b>: Links with data-prefetch are fetched on hover</li>
 *   <li><b>Partial Swap</b>: Only updates specified elements</li>
 *   <li><b>History</b>: Proper browser back/forward support</li>
 *   <li><b>Progressive</b>: Falls back gracefully without JS</li>
 * </ul>
 *
 * @see Link
 */
public final class Navigation {

    private Navigation() {}

    /**
     * Returns the navigation JavaScript as a string.
     *
     * @return the JavaScript code
     */
    public static String js() {
        return """
            (function() {
                'use strict';

                const cache = new Map();
                const CACHE_TTL = 60000; // 1 minute

                // Prefetch on hover
                document.addEventListener('mouseover', function(e) {
                    const link = e.target.closest('a[data-prefetch]');
                    if (!link) return;

                    const href = link.getAttribute('href');
                    if (!href || href.startsWith('#') || href.startsWith('javascript:')) return;
                    if (cache.has(href)) return;

                    // Prefetch the page
                    fetch(href, {
                        headers: { 'X-Prefetch': 'true' }
                    })
                    .then(r => r.text())
                    .then(html => {
                        cache.set(href, { html, time: Date.now() });
                        // Clean old entries
                        for (const [key, val] of cache) {
                            if (Date.now() - val.time > CACHE_TTL) cache.delete(key);
                        }
                    })
                    .catch(() => {});
                });

                // Handle navigation clicks
                document.addEventListener('click', function(e) {
                    const link = e.target.closest('a[data-swap], a[data-prefetch]');
                    if (!link) return;

                    // Skip if modifier keys
                    if (e.ctrlKey || e.metaKey || e.shiftKey) return;

                    const href = link.getAttribute('href');
                    if (!href || href.startsWith('#') || href.startsWith('javascript:')) return;

                    // Skip external links
                    try {
                        const url = new URL(href, window.location.origin);
                        if (url.origin !== window.location.origin) return;
                    } catch { return; }

                    e.preventDefault();
                    navigate(href, link);
                });

                // Handle browser back/forward
                window.addEventListener('popstate', function(e) {
                    if (e.state && e.state.jweb) {
                        navigate(window.location.pathname, null, false);
                    }
                });

                async function navigate(href, link, pushState = true) {
                    const swapTarget = link?.getAttribute('data-swap');
                    const swapStrategy = link?.getAttribute('data-swap-strategy') || 'innerHTML';
                    const shouldPush = link?.getAttribute('data-push-state') !== 'false';

                    try {
                        // Check cache first
                        let html;
                        const cached = cache.get(href);
                        if (cached && Date.now() - cached.time < CACHE_TTL) {
                            html = cached.html;
                        } else {
                            const response = await fetch(href, {
                                headers: { 'X-Navigation': 'true' }
                            });
                            if (!response.ok) {
                                window.location.href = href;
                                return;
                            }
                            html = await response.text();
                            cache.set(href, { html, time: Date.now() });
                        }

                        // Parse the response
                        const parser = new DOMParser();
                        const doc = parser.parseFromString(html, 'text/html');

                        if (swapTarget) {
                            // Partial swap
                            const target = document.querySelector(swapTarget);
                            const source = doc.querySelector(swapTarget);

                            if (target && source) {
                                switch (swapStrategy) {
                                    case 'outerHTML':
                                        target.outerHTML = source.outerHTML;
                                        break;
                                    case 'beforeend':
                                        target.insertAdjacentHTML('beforeend', source.innerHTML);
                                        break;
                                    case 'afterbegin':
                                        target.insertAdjacentHTML('afterbegin', source.innerHTML);
                                        break;
                                    default:
                                        target.innerHTML = source.innerHTML;
                                }
                            }

                            // Update title
                            const newTitle = doc.querySelector('title');
                            if (newTitle) document.title = newTitle.textContent;
                        } else {
                            // Full page swap
                            document.documentElement.innerHTML = doc.documentElement.innerHTML;

                            // Re-run scripts
                            doc.querySelectorAll('script').forEach(oldScript => {
                                const newScript = document.createElement('script');
                                Array.from(oldScript.attributes).forEach(attr => {
                                    newScript.setAttribute(attr.name, attr.value);
                                });
                                newScript.textContent = oldScript.textContent;
                                document.body.appendChild(newScript);
                            });
                        }

                        // Update URL
                        if (pushState && shouldPush) {
                            history.pushState({ jweb: true }, '', href);
                        }

                        // Scroll to top
                        window.scrollTo(0, 0);

                        // Update active states
                        updateActiveLinks(href);

                        // Dispatch event
                        window.dispatchEvent(new CustomEvent('jweb:navigate', { detail: { href } }));

                    } catch (err) {
                        console.error('Navigation failed:', err);
                        window.location.href = href;
                    }
                }

                function updateActiveLinks(currentPath) {
                    document.querySelectorAll('a[data-active-class]').forEach(link => {
                        const activeClass = link.getAttribute('data-active-class');
                        const href = link.getAttribute('href');

                        if (href === currentPath || currentPath.startsWith(href + '/')) {
                            link.classList.add(activeClass);
                        } else {
                            link.classList.remove(activeClass);
                        }
                    });
                }

                // Initial state
                history.replaceState({ jweb: true }, '', window.location.pathname);
                updateActiveLinks(window.location.pathname);

                // Expose API
                window.JWebNav = {
                    navigate: (href) => navigate(href, null, true),
                    prefetch: (href) => {
                        fetch(href, { headers: { 'X-Prefetch': 'true' } })
                            .then(r => r.text())
                            .then(html => cache.set(href, { html, time: Date.now() }));
                    },
                    clearCache: () => cache.clear()
                };
            })();
            """;
    }

    /**
     * Returns an inline script element with the navigation runtime.
     *
     * @return a script element
     */
    public static Element script() {
        return inlineScript(js());
    }

    /**
     * Returns a minified version of the navigation script.
     *
     * @return minified JavaScript
     */
    public static String minifiedJs() {
        // For production, you'd want a proper minifier
        // This is a simple version that removes comments and extra whitespace
        return js()
            .replaceAll("//.*\\n", "\n")
            .replaceAll("/\\*.*?\\*/", "")
            .replaceAll("\\s+", " ")
            .replaceAll(" ?([{}();,]) ?", "$1")
            .trim();
    }
}
