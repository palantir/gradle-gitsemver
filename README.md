# gitsemver plugin for gradle

## Why?

To create an alternative to using `git describe` for automatic versioning that works nicely with git flow and automatically updates project versions.

## How does it work?

1. Find all tags of format v?\d+\.\d+\.\d+ accessable from HEAD
2. Use a slightly modified semantic versioning scheme to sort tags
3. Choose the 'largest' tag and append to the build portion of version if tag != HEAD:
   * number of commits since that tag
   * the git hash in the format g01ABCDEF
   * the dirty state of the repo
4. Use the tag itself without any appendix if tag == HEAD

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

Sure. Standard semver sorts words alphabetically. This is not wanted if you're going to be creating tags like `v0.1.0-dev` and `v0.1.0-alpha`. You want to alpha sort everything except 'dev', 'alpha', 'beta', and 'rc', where those are ordered and always bigger than any other word. Thats it.

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
$
```

## Prefix tags

gradle-gitsemver also supports a special mode of operatino where it looks for tags with a given prefix. This can be done using the `prefixSemverVersion("prefix")` convention:

```
apply plugin: 'gitsemver'
version prefixSemverVersion("projecta")
```

This will look for all tags with form `projecta-v1.2.3` and ignore everything else. If there are no tags of this form in the repo, it will error out.

This is useful in cases in which multiple subprojects need to be independently versioned.
