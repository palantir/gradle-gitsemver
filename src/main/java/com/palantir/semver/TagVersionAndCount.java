package com.palantir.semver;

public class TagVersionAndCount {

    private final String version;
    private final int count;

    public TagVersionAndCount(String version, int count) {
        this.version = version;
        this.count = count;
    }

    public String getVersion() {
        return version;
    }

    public int getCount() {
        return count;
    }

}
