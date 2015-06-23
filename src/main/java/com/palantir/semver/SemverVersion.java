package com.palantir.semver;

public class SemverVersion {

    private final String fullVersion;
    private final String tagName;
    private final String headCommitHash;
    private final Integer count;
    private final Integer buildNumber;
    private final boolean dirty;
    
    public SemverVersion (String fullVersion, String tagName, String headCommitHash,
            Integer count, Integer buildNumber, boolean dirty) {
        this.fullVersion = fullVersion;
        this.tagName = tagName;
        this.headCommitHash = headCommitHash;
        this.count = count;
        this.buildNumber = buildNumber;
        this.dirty = dirty;
    }
    
    public String toString() {
        return this.fullVersion;
    }
    
    public String getTagName() {
        return this.tagName;
    }
    
    public String getHeadCommitHash() {
        return this.headCommitHash;
    }
    
    public Integer getCommitCount() {
        return this.count;
    }
    
    public Integer getBuildNumber() {
        return this.buildNumber;
    }
    
    public boolean getDirty() {
        return this.dirty;
    }
}
