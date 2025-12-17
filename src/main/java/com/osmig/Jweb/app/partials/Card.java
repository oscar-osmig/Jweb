package com.osmig.Jweb.app.partials;

import com.osmig.Jweb.framework.core.Element;
import com.osmig.Jweb.framework.template.Template;
import static com.osmig.Jweb.framework.elements.Elements.*;

/**
 * Reusable card component.
 */
public class Card implements Template {

    private final String title;
    private final String description;

    public Card(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public static Card create(String title, String description) {
        return new Card(title, description);
    }

    @Override
    public Element render() {
        return div(class_("card"), style_("background: white; border-radius: 8px; padding: 20px; margin: 10px 0; box-shadow: 0 2px 4px rgba(0,0,0,0.1);"),
        h3(style_("color: #667eea; margin-bottom: 8px;"), title),
        p(description)
    );
    }
}
