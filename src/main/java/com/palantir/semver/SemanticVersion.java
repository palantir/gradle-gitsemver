package com.palantir.semver;

public interface SemanticVersion extends Comparable<SemanticVersion> {

    String getOriginalVersion();

    int getMajorVersion();

    int getMinorVersion();

    int getPatchVersion();

    String getReleaseCandidate();

    String getMetadata();

}