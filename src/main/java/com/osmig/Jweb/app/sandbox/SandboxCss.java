package com.osmig.Jweb.app.sandbox;

import jweb.Cls;
import jweb.Id;
import jweb.Style;
import jweb.css.Selector;

import static jweb.El.*;
import static jweb.Css.*;

/**
 * The sandbox page's class and id handles — each name typed once, shared by
 * the markup in {@link SandboxPage}, its stylesheet and {@link SandboxScript}.
 * The fragment markup's handles ({@code rx-*}, knobs, chips) live in
 * {@link SandboxPanes}, where those elements are rendered.
 */
public final class SandboxCss {
    private SandboxCss() {}

    // ==================== page layout ====================

    public static final Cls LAYOUT = cls("sandbox-layout");
    public static final Cls PANES = cls("sandbox-panes");
    public static final Id DYNBAR = id("sandbox-dynbar");
    public static final Cls KNOBS = cls("sandbox-knobs");

    // ==================== code / preview split ====================
    // The panes the script addresses carry an id for it and a class for the
    // stylesheet, so each has both handles.

    public static final Id SPLIT = id("sandbox-split");
    public static final Cls SPLIT_CLS = cls("sandbox-split");
    public static final Id CODE = id("sandbox-code");
    public static final Cls CODE_CLS = cls("sandbox-code");
    public static final Cls CODE_HEAD = cls("sandbox-code-head");
    public static final Id TREE_TOGGLE = id("sandbox-tree-toggle");
    public static final Cls TREE_TOGGLE_CLS = cls("sandbox-tree-toggle");
    public static final Id PATH = id("sandbox-path");
    public static final Cls EDITWRAP = cls("sandbox-editwrap");
    public static final Id LINES = id("sandbox-lines");
    public static final Cls LINES_CLS = cls("sandbox-lines");
    public static final Id EDITOR = id("sandbox-editor");
    public static final Cls EDITOR_CLS = cls("sandbox-editor");
    public static final Id MIRROR = id("sandbox-mirror");
    public static final Cls MIRROR_CLS = cls("sandbox-mirror");
    public static final Id STATUS = id("sandbox-status");
    public static final Cls STATUS_CLS = cls("sandbox-status");
    public static final Id GUTTER = id("sandbox-gutter");
    public static final Cls GUTTER_CLS = cls("sandbox-gutter");
    public static final Id PREVIEW = id("sandbox-preview");
    public static final Cls PREVIEW_CLS = cls("sandbox-preview");
    public static final Cls PREVIEW_HEAD = cls("sandbox-preview-head");
    public static final Cls DOT = cls("sandbox-dot");
    public static final Cls DOT_R = cls("sandbox-dot-r");
    public static final Cls DOT_Y = cls("sandbox-dot-y");
    public static final Cls DOT_G = cls("sandbox-dot-g");
    public static final Cls URL = cls("sandbox-url");
    public static final Cls STAGE = cls("sandbox-stage");
    public static final Id VIEW = id("sandbox-view");

    // ==================== file tree ====================

    public static final Cls TREE = cls("sandbox-tree");
    public static final Cls TREE_INNER = cls("sandbox-tree-inner");
    public static final Cls TREE_FOOT = cls("sandbox-tree-foot");
    public static final Id TREE_COLLAPSE = id("sandbox-tree-collapse");
    public static final Cls TREE_COLLAPSE_CLS = cls("sandbox-tree-collapse");
    public static final Cls FOLDER = cls("sandbox-folder");
    public static final Cls CHEV = cls("sandbox-chev");
    public static final Cls KIDS = cls("sandbox-kids");
    public static final Cls FILE = cls("sandbox-file");
    public static final Cls DEPTH_0 = cls("sandbox-depth-0");
    public static final Cls DEPTH_1 = cls("sandbox-depth-1");
    public static final Cls DEPTH_2 = cls("sandbox-depth-2");
    public static final Cls DEPTH_3 = cls("sandbox-depth-3");

    /** The indent class for a tree row's nesting depth. */
    public static Cls depth(int depth) {
        return switch (depth) {
            case 0 -> DEPTH_0;
            case 1 -> DEPTH_1;
            case 2 -> DEPTH_2;
            case 3 -> DEPTH_3;
            default -> throw new IllegalArgumentException("tree depth " + depth + " has no class");
        };
    }

    // ==================== "Add snippet" panel ====================

    public static final Cls SNIPPET = cls("sandbox-snippet");
    public static final Id SNIPPET_PANEL = id("snippet-panel");
    public static final Id SNIPPET_FORM = id("snippet-form");
    public static final Cls SNIPPET_FORM_CLS = cls("sandbox-snippet-form");
    public static final Id SNIPPET_CODE = id("snippet-code");
    public static final Cls SNIPPET_FIELD = cls("sandbox-snippet-field");
    public static final Id SNIPPET_TITLE = id("snippet-title");
    public static final Id SNIPPET_AUTHOR = id("snippet-author");
    public static final Id SNIPPET_CANCEL = id("snippet-cancel");
    public static final Cls SNIPPET_HINT = cls("sandbox-snippet-hint");
    public static final Id SNIPPET_STATUS = id("snippet-status");

    // ==================== state classes ====================
    // Toggled by the script; the last two are set by the framework's
    // splitPane and lineGutter behaviours.

    public static final Cls ACTIVE = cls("active");          // the open file
    public static final Cls CLOSED = cls("closed");          // a folded folder
    public static final Cls COLLAPSED = cls("collapsed");    // its hidden children
    public static final Cls TREE_HIDDEN = cls("hidden");     // the tree, toggled away
    public static final Cls OPEN = cls("open");              // the snippet panel
    public static final Cls OK = cls("ok");                  // status colours
    public static final Cls ERR = cls("err");
    public static final Cls MAX = cls("sandbox-max");        // full-screen preview
    public static final Cls SHAKE = cls("sandbox-shake");    // the red light's "no"
    public static final Cls DRAGGING = cls("dragging");      // splitPane, mid-drag
    public static final Cls ERRLINE = cls("errline");        // lineGutter's marked line

    /** The compound selector for {@code base} while it carries {@code state}: {@code .a.b}. */
    public static Selector with(Selector base, Cls state) {
        return base.cls(state.name());
    }

    /** The editor's font stack, for {@code .apply(mono())}. */
    public static Style<?> mono() {
        return style().fontFamily(uiMonospace, font("SFMono-Regular"), font("Menlo"), monospace);
    }
}
