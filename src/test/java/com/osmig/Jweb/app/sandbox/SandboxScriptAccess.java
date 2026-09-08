package com.osmig.Jweb.app.sandbox;

/** Test-only bridge to the package-private editor script. */
public final class SandboxScriptAccess {
    private SandboxScriptAccess() {}

    public static String build() {
        return SandboxScript.build();
    }
}
