package jweb;

/**
 * Short alias for {@link com.osmig.Jweb.framework.seo.Seo} — same API,
 * shorter import: {@code import jweb.Seo;}
 */
@SuppressWarnings("deprecation")
public class Seo extends com.osmig.Jweb.framework.seo.Seo {

    protected Seo(String title, String description) {
        super(title, description);
    }
}
