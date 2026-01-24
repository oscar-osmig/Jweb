package com.osmig.Jweb.app.subheader;

/**
 * JavaScript for dynamically populating the subheader sidebar.
 * Scans h2 and h3 elements in the docs content and creates navigation links.
 */
public final class SubheaderScript {
    private SubheaderScript() {}

    public static String build() {
        return """
            (function() {
                function initSubheader() {
                    const sidebar = document.getElementById('subheader-sidebar');
                    const nav = document.getElementById('subheader-nav');
                    const content = document.querySelector('.docs-content');

                    if (!sidebar || !nav || !content) return;

                    const headers = content.querySelectorAll('h2, h3');
                    console.log('Found ' + headers.length + ' headers in .docs-content');

                    if (headers.length === 0) {
                        sidebar.style.display = 'none';
                        return;
                    }

                    sidebar.style.display = 'block';
                    nav.innerHTML = '';

                    const headerArray = Array.from(headers);
                    let clickLock = false;

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

                        link.style.cssText = 'display: block; padding: ' + padding + '; border-radius: 6px; font-size: ' + fontSize + '; color: #64748b; text-decoration: none; transition: all 0.15s ease;';

                        link.addEventListener('click', function(e) {
                            e.preventDefault();
                            const target = document.getElementById(id);
                            if (target) {
                                clickLock = true;
                                const targetRect = target.getBoundingClientRect();
                                const contentRect = content.getBoundingClientRect();
                                const offset = targetRect.top - contentRect.top - 55;
                                content.scrollBy({ top: offset, behavior: 'smooth' });
                                history.pushState(null, null, '#' + id);
                                setActiveLink(index);
                                setTimeout(function() { clickLock = false; }, 500);
                            }
                        });

                        link.addEventListener('mouseenter', function() {
                            this.style.color = '#6366f1';
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
                            activeLink.style.color = '#6366f1';
                            activeLink.style.backgroundColor = '#eef2ff';
                            activeLink.style.fontWeight = '600';
                            activeLink.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
                        }
                    }

                    function findActiveHeader() {
                        if (clickLock) return;
                        const contentRect = content.getBoundingClientRect();

                        // Find the header closest to the top of the visible area
                        let activeIndex = 0;
                        let bestDistance = Infinity;

                        for (let i = 0; i < headerArray.length; i++) {
                            const header = headerArray[i];
                            const rect = header.getBoundingClientRect();
                            const relativeTop = rect.top - contentRect.top;

                            // Header is at or above the threshold - it's a candidate
                            if (relativeTop <= 70) {
                                activeIndex = i;
                            }
                        }

                        // If no header has crossed the threshold yet, pick the first visible one
                        if (activeIndex === 0) {
                            for (let i = 0; i < headerArray.length; i++) {
                                const header = headerArray[i];
                                const rect = header.getBoundingClientRect();
                                const relativeTop = rect.top - contentRect.top;

                                if (relativeTop >= 0 && relativeTop < bestDistance) {
                                    bestDistance = relativeTop;
                                    activeIndex = i;
                                }
                            }
                        }

                        setActiveLink(activeIndex);
                    }

                    // Listen for scroll events
                    content.addEventListener('scroll', findActiveHeader);

                    // Set initial active state
                    setTimeout(findActiveHeader, 100);
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
