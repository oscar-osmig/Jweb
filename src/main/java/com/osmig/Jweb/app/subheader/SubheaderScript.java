package com.osmig.Jweb.app.subheader;

/**
 * JavaScript for dynamically populating the subheader sidebar.
 * Scans h2 and h3 elements in the docs content and creates navigation links.
 *
 * Scrollspy rules:
 * - the "active" header is the last one above an activation line placed 25%
 *   down the visible content area (not a fixed pixel offset), so the section
 *   occupying the viewport is the one highlighted;
 * - at the very bottom of the scroll container the last header wins (it may
 *   never reach the activation line otherwise);
 * - the sidebar rebuilds itself whenever the docs content is swapped by the
 *   left sidebar's SPA navigation (MutationObserver), so links never point at
 *   detached DOM nodes.
 */
public final class SubheaderScript {
    private SubheaderScript() {}

    public static String build() {
        return """
            (function() {
                let headerArray = [];
                let clickLock = false;
                let sidebar, nav, content;

                function buildLinks() {
                    const headers = content.querySelectorAll('h2, h3');
                    headerArray = Array.from(headers);

                    if (headerArray.length === 0) {
                        sidebar.classList.remove('has-headers');
                        return;
                    }

                    sidebar.classList.add('has-headers');
                    nav.innerHTML = '';

                    headerArray.forEach(function(header, index) {
                        const id = header.id || 'section-' + index;
                        if (!header.id) header.id = id;

                        const link = document.createElement('a');
                        link.href = '#' + id;
                        link.textContent = header.textContent;
                        link.className = 'subheader-link';
                        link.dataset.index = index;

                        const isH3 = header.tagName === 'H3';
                        const padding = isH3 ? '0.5rem 0.75rem 0.5rem 1.5rem' : '0.5rem 0.75rem';
                        const fontSize = isH3 ? '0.8rem' : '0.875rem';

                        // overflow-wrap keeps a heading whose single longest token
                        // is wider than the rail (a path, a URL) from pushing that
                        // token to its own line and stranding the number above it.
                        link.style.cssText = 'display: block; overflow-wrap: break-word; padding: ' + padding + '; border-radius: 6px; font-size: ' + fontSize + '; color: #64748b; text-decoration: none; transition: all 0.15s ease;';

                        link.addEventListener('click', function(e) {
                            e.preventDefault();
                            const target = document.getElementById(id);
                            if (target) {
                                clickLock = true;
                                const targetRect = target.getBoundingClientRect();
                                const contentRect = content.getBoundingClientRect();
                                const offset = targetRect.top - contentRect.top - 24;
                                content.scrollBy({ top: offset, behavior: 'smooth' });
                                history.pushState(null, null, '#' + id);
                                setActiveLink(index);
                                releaseClickLockOnSettle();
                            }
                        });

                        link.addEventListener('mouseenter', function() {
                            this.style.color = '#4f46e5';
                            this.style.backgroundColor = '#eef2ff';
                        });
                        link.addEventListener('mouseleave', function() {
                            if (!this.classList.contains('active')) {
                                this.style.color = '#64748b';
                                this.style.backgroundColor = 'transparent';
                            }
                        });

                        nav.appendChild(link);
                    });

                    findActiveHeader();
                }

                // Release the click lock only once smooth scrolling has settled,
                // so the spy doesn't fight the clicked link mid-animation.
                let settleTimer = null;
                function releaseClickLockOnSettle() {
                    let lastTop = content.scrollTop;
                    clearInterval(settleTimer);
                    settleTimer = setInterval(function() {
                        if (content.scrollTop === lastTop) {
                            clearInterval(settleTimer);
                            clickLock = false;
                        }
                        lastTop = content.scrollTop;
                    }, 120);
                }

                function setActiveLink(index) {
                    const links = nav.querySelectorAll('.subheader-link');
                    links.forEach(function(link) {
                        link.classList.remove('active');
                        link.style.color = '#64748b';
                        link.style.backgroundColor = 'transparent';
                        link.style.fontWeight = '400';
                    });
                    const activeLink = links[index];
                    if (activeLink) {
                        activeLink.classList.add('active');
                        activeLink.style.color = '#4f46e5';
                        activeLink.style.backgroundColor = '#eef2ff';
                        activeLink.style.fontWeight = '600';
                        activeLink.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                    }
                }

                function findActiveHeader() {
                    if (clickLock || headerArray.length === 0) return;

                    // Bottom of the scroll container: the last section wins
                    if (content.scrollTop + content.clientHeight >= content.scrollHeight - 4) {
                        setActiveLink(headerArray.length - 1);
                        return;
                    }

                    // Activation line 25% down the visible area (max 160px):
                    // the active header is the last one at or above it.
                    const contentRect = content.getBoundingClientRect();
                    const activationLine = Math.min(contentRect.height * 0.25, 160);

                    let activeIndex = 0;
                    for (let i = 0; i < headerArray.length; i++) {
                        const relativeTop = headerArray[i].getBoundingClientRect().top - contentRect.top;
                        if (relativeTop <= activationLine) {
                            activeIndex = i;
                        } else {
                            break;
                        }
                    }

                    setActiveLink(activeIndex);
                }

                function initSubheader() {
                    sidebar = document.getElementById('subheader-sidebar');
                    nav = document.getElementById('subheader-nav');
                    content = document.querySelector('.docs-content');

                    if (!sidebar || !nav || !content) return;

                    buildLinks();

                    content.addEventListener('scroll', findActiveHeader, { passive: true });

                    // Rebuild when SPA navigation swaps the docs content
                    new MutationObserver(function() {
                        clickLock = false;
                        buildLinks();
                    }).observe(content, { childList: true });
                }

                if (document.readyState === 'loading') {
                    document.addEventListener('DOMContentLoaded', initSubheader);
                } else {
                    initSubheader();
                }
            })();
            """;
    }
}
