What you want is to publish it to a **Maven repository** — then anyone can pull it into their project with a few lines in their `pom.xml` or `build.gradle`, and Maven/Gradle handles the download automatically. You have three realistic options, easiest first.

**Option 1: JitPack (easiest, ~10 minutes)**

JitPack builds directly from your GitHub repo. No accounts, no signing, no publishing config.

1. Push your framework to a public GitHub repo (it needs a working `pom.xml` or `build.gradle`)
2. Create a release/tag, e.g. `v2.0.0` (the current JWeb release)
3. That's it. Users add:

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.YourUsername</groupId>
    <artifactId>your-repo-name</artifactId>
    <version>v2.0.0</version>
</dependency>
```

JitPack compiles your repo on first request and caches the artifact. The only downside is users have to add the extra `<repository>` block, and builds can occasionally be flaky.

**Option 2: Maven Central (the "official" way)**

This is where Spring, Guava, etc. live — users need zero extra config, just the `<dependency>` block. It's more setup:

1. Register at the Sonatype Central Portal (central.sonatype.com)
2. Verify you own your namespace — if you don't own a domain, you can use `io.github.yourusername` and prove it by creating a temporary GitHub repo they name
3. Generate a GPG key and sign your artifacts
4. Configure the `central-publishing-maven-plugin` (or Gradle equivalent) plus source/javadoc jar plugins, since Central requires sources, javadocs, and POM metadata (license, SCM URL, developer info)
5. Run `mvn deploy` and release through the portal

First-time setup takes an hour or two; after that, releasing is one command. Worth it if you want the framework to feel like a serious public library.

**What is already in place (2026-10-10):** the `pom.xml` carries the metadata Central
checks (`name`, `description`, `url`, `licenses`, `scm`, `developers`) and a `central`
profile that adds the javadoc jar, GPG signing and the Central publishing plugin. The default
build is untouched. What remains is yours to do once:

1. **Namespace.** Central must verify the `groupId`. `com.osmig` needs the domain
   `osmig.com` (a DNS TXT record); otherwise change the groupId to `io.github.oscar-osmig`
   and verify it through a throwaway GitHub repo the portal names. JitPack coordinates
   (`com.github.oscar-osmig:Jweb`) are unaffected either way.
2. **Token.** Generate a portal token at central.sonatype.com and put it in
   `~/.m2/settings.xml`:

   ```xml
   <servers>
     <server><id>central</id><username>TOKEN_USER</username><password>TOKEN_PASS</password></server>
   </servers>
   ```

3. **GPG key.** `gpg --gen-key`, publish it (`gpg --keyserver keyserver.ubuntu.com --send-keys KEYID`),
   and export the passphrase as `MAVEN_GPG_PASSPHRASE` when deploying.
4. **Deploy a tagged release:** `./mvnw -B -Pcentral -DskipTests deploy`. The profile
   uploads and validates the bundle (`waitUntil=validated`); the final *Publish* click is on
   the portal, so a bad bundle never reaches Central.

A dry run without credentials — `./mvnw -B -Pcentral -DskipTests -Dgpg.skip package` — builds
the javadoc jar beside the sources jar, which is what CI can check.

**What a consumer pulls in.** The library's non-optional dependencies are the Spring Boot web
and websocket starters, Jackson, the MongoDB driver (`mongodb-driver-sync`) and
`spring-dotenv`. BCrypt (`spring-security-crypto`) and JWT (`jjwt`) are `optional`: an app
that uses `Password` or `Jwt` adds them itself. Spring Data JPA, Spring AI and Testcontainers
appear in the pom only as commented-out examples — they are not dependencies.

**Option 3: GitHub Packages**

Publishes to a Maven registry tied to your repo, but users need a GitHub token even to *read* public packages, which is annoying enough that I'd skip it for a public framework.

**My recommendation:** start with JitPack today to make it usable immediately, and move to Maven Central once the framework stabilizes and you want a cleaner install story. If you tell me whether you're using Maven or Gradle, I can give you the exact publishing config for either route.

---

## Cutting a release

A tag is cut only for framework changes (new or changed syntax, runtime, dependencies);
sample-app and docs work is committed to `main` without one. CI
(`.github/workflows/ci.yml`) must be green on the commit you tag — JitPack and the
Dockerfile both build with `-DskipTests`, so the workflow is the only automatic gate.

1. **Bump the version** everywhere it is written: `pom.xml`; the README header and the two
   install snippets; the head of `DocVersions.ALL` (the previous version stays — it is
   tagged); `DocVersionsTest` (latest / normalize / "(latest)" / "Switch to" / third-newest);
   the compatibility note in `DocsTell`. New in-app docs prose goes in
   `since("vX.Y.Z", …)` blocks so the version picker gates it.
2. **Write the `CHANGELOG.md` section** for the version (move the `[Unreleased]` bullets
   under it, add the date and the compare link). If the release has a migration guide at the
   repo root, add it to the `COPY` line in the Dockerfile and to the README documentation
   table — a guide DocsTell serves but the image does not carry is a 500 in production.
3. **Run the suite** (`sh mvnw -B verify`), commit, tag (annotated, `vX.Y.Z`), push the commit
   and the tag.
4. **Publish the GitHub Release from the changelog section**:

   ```bash
   ./tools/release-notes.sh 3.0.3 > /tmp/notes.md     # extracts the [3.0.3] section
   gh release create v3.0.3 --verify-tag --title "v3.0.3" --notes-file /tmp/notes.md
   ```

5. **Check JitPack** built the tag (`https://jitpack.io/#oscar-osmig/Jweb/vX.Y.Z`) and that
   production serves it (`curl -sI https://jweb.build | grep -i x-jweb-version`).