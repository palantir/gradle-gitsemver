package com.palantir.semver;

public class SemverVersion {

    private final String fullVersion;
    private final String tagName;
    private final String gitHash;
    private final Integer commitCountFromTag;
    private final Integer buildNumber;
    private final boolean dirty;
    
    public SemverVersion (String fullVersion, String tagName, String gitHash,
            Integer commitCountFromTag, Integer buildNumber, boolean dirty) {
        this.fullVersion = fullVersion;
        this.tagName = tagName;
        this.gitHash = gitHash;
        this.commitCountFromTag = commitCountFromTag;
        this.buildNumber = buildNumber;
        this.dirty = dirty;
    }
    
    public String toString() {
        return this.fullVersion;
    }
    
    public String getTagName() {
        return this.tagName;
    }
    
    public String getGitHash() {
        return this.gitHash;
    }
    
    public Integer getCommitCount() {
        return this.commitCountFromTag;
    }
    
    public Integer getBuildNumber() {
        return this.buildNumber;
    }
    
    public boolean getDirty() {
        return this.dirty;
    }
}
