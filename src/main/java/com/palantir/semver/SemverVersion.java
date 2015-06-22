package com.palantir.semver;

public class SemverVersion {

    private final String fullVersion;
    private final String tagVersion;
    private final String headCommitHash;
    private final Integer count;
    private final Integer buildNumber;
    private final boolean isDirty;
    private final boolean isVersionStableRelease;
    
    public SemverVersion (String fullVersion, String tagVersion, String headCommitHash,
            Integer count, Integer buildNumber, boolean isDirty, boolean isVersionStableRelease) {
        this.fullVersion = fullVersion;
        this.tagVersion = tagVersion;
        this.headCommitHash = headCommitHash;
        this.count = count;
        this.buildNumber = buildNumber;
        this.isDirty = isDirty;
        this.isVersionStableRelease = isVersionStableRelease;
    }
    
    public String toString() {
        return this.fullVersion;
    }
    
    public String getTagVersion() {
        return this.tagVersion;
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
    
    public boolean isDirty() {
        return this.isDirty;
    }
    
    public boolean isVersionStableRelease() {
        return this.isVersionStableRelease;
    }
    
}
