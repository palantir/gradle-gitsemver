# Gitsemver plugin for gradle [ ![Download](https://api.bintray.com/packages/palantir/maven/gradle-gitsemver/images/download.svg) ](https://bintray.com/palantir/maven/gradle-gitsemver/_latestVersion)

## Why?

To create an alternative to using `git describe` for automatic versioning that works nicely with git flow and automatically updates project versions.

## How does it work?

1. Finds all tags of format v?\d+\.\d+\.\d+ accessible from HEAD
2. Uses a slightly modified semantic versioning scheme to sort tags
3. Chooses the 'largest' tag
4. If the tag is not at HEAD, it appends to the version:
   * the number of commits since that tag
   * the git hash in the format g01ABCDEF
   * the dirty state of the repo
4. If the tag is at HEAD, then nothing is appended

### Example:

Suppose we have a git history that looks like this (newest on top):

```
* eeeeeee - (HEAD, develop) fix a bug <EA>
* ddddddd - merge 'feature/stuff' into 'develop' <EA>
|\
| * ccccccc - (feature/stuff) my feature is done <EA>
* | bbbbbbb - (tag: v0.1.0-dev) preparing develop branch <EA>
|/
* aaaaaaa - (master, tag: v0.0.0) Initial commit <EA>
```

For each ref that I could checkout, the version would be:

* `master`: v0.0.0
* `bbbbbb`: v0.1.0-dev
* `feature/stuff`: v0.0.0-1-gccccccc
* `ddddddd`: v0.1.0-dev-2-gddddddd
* `eeeeeee`: v0.1.0-dev-3-geeeeeee

Finally, if I were on the develop branch and had uncommitted changes the version would be v0.1.0-dev+3.geeeeeee.dirty

### Can you explain your modified semver sort?

Sure. Standard semantic versioning sorts words alphabetically. This is not wanted if you're going to be creating tags like `v0.1.0-dev` and `v0.1.0-alpha`. You want to alpha sort everything except 'dev', 'alpha', 'beta', and 'rc', where those are ordered and always bigger than any other word. Thats it.

## Adding to your build

```
buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath 'com.palantir:gradle-gitsemver:0.1.2'
  }
}

apply plugin: 'gitsemver'
version semverVersion()
```

Now verify that the version is being applied:

```
$ gradle properties | grep version
version: v0.0.0-58-g5f78071.dirty
```

## Prefix tags

Gitsemver supports a special mode of operation where it looks for tags with a given prefix. This can be done using the `prefixSemverVersion("prefix")` convention:

```
apply plugin: 'gitsemver'
version prefixSemverVersion("projecta")
```

This will look for all tags with form `projecta-v1.2.3` and ignore everything else. If there are no tags of this form in the repo, it will error out.

This is useful in cases in which multiple subprojects need to be independently versioned.

## Topological Semver

It's also possible to have the tags sorted by how far from HEAD they are. To use the topological sorting, copy this into your build file:

```
apply plugin: 'gitsemver'
version topoSemverVersion("prefix")
```
where prefix is the prefix for the tags you want to search (see Prefix tags).

Topological sorting will then find the closest tag to HEAD that also has matches the prefix, and use that tag as the base for the version.

## Version Object

The plugin's version methods return a ``SemverVersion`` object and not a ``String``.  It can be used as a parameter for Gradle's `version`.  Making this an object allows for pulling out pieces of the version string for other uses (if desired).

```java
class SemverVersion {
  String toString()  // The full version string
  String getTagVersion() // The matched tagged
  String getHeadCommitHash() // The git commit hash of the HEAD commit
  Integer getCommitCount() // The number of commits HEAD is from the matched tag
  Integer getBuildNumber() // The value of the BUILD_NUMBER environment variable
  boolean isDirty() // Is the git repo dirty?
  boolean isVersionStableRelease() // Does the full version represent a stable version?
}
```
