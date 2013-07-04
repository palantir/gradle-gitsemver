package com.palantir.semver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DefaultSemanticVersion implements SemanticVersion {

    private static final String SEMANTIC_VERSION_REGEX = createSemanticVersionRegex();
    private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile("^"
            + SEMANTIC_VERSION_REGEX + "$");
    private static final SemanticVersionComparator COMPARATOR = new SemanticVersionComparator();

    private static String createSemanticVersionRegex() {
        String majorMinorPatchPattern = "(\\d+)(\\.(\\d+))(\\.(\\d+))";
        String releaseCandidatePattern = "(-(([0-9A-Za-z-]+)(\\.[0-9A-Za-z-]+)*))?";
        String buildPattern = "(\\+(([0-9A-Za-z-]+)(\\.[0-9A-Za-z-]+)*))?";
        return majorMinorPatchPattern + releaseCandidatePattern + buildPattern;
    }

    private final String originalVersion;
    private final int major;
    private final int minor;
    private final int patch;
    // This can be null
    private final String releaseCandidate;
    // This can be null
    private final String metadata;

    /**
     * This constructor is package private for the purposes of testing
     *
     * @param version
     *
     * @param releaseCandidate is nullable
     * @param metadata is nullable
     */
    DefaultSemanticVersion(String originalVersion,
                           int major,
                           int minor,
                           int patch,
                           String releaseCandidate,
                           String metadata) {
        this.originalVersion = originalVersion;
        this.major = checkNonNegativeVersion(major);
        this.minor = checkNonNegativeVersion(minor);
        this.patch = checkNonNegativeVersion(patch);
        this.releaseCandidate = releaseCandidate;
        this.metadata = metadata;
    }

    public static SemanticVersion createFromString(String version) {
        Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(version);
        checkArgument(matcher.matches(), "Version string " + version
                + " is not a semantic version");
        int major = Integer.parseInt(matcher.group(1));
        int minor = Integer.parseInt(matcher.group(3));
        int patch = Integer.parseInt(matcher.group(5));
        String releaseCandidate = matcher.group(7);
        String build = matcher.group(11);
        return new DefaultSemanticVersion(
                version,
                major,
                minor,
                patch,
                releaseCandidate,
                build);
    }

    private static int checkNonNegativeVersion(int version) {
        checkArgument(
                version >= 0,
                "major, minor, and patch versions cannot be negative");
        return version;
    }

    public static boolean isValid(String version) {
        return SEMANTIC_VERSION_PATTERN.matcher(version).matches();
    }

    @Override
    public String getOriginalVersion() {
        return originalVersion;
    }

    @Override
    public int getMajorVersion() {
        return major;
    }

    @Override
    public int getMinorVersion() {
        return minor;
    }

    @Override
    public int getPatchVersion() {
        return patch;
    }

    @Override
    public String getReleaseCandidate() {
        return releaseCandidate;
    }

    @Override
    public String getMetadata() {
        return metadata;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((metadata == null) ? 0 : metadata.hashCode());
        result = prime * result + major;
        result = prime * result + minor;
        result = prime * result + patch;
        result = prime
                * result
                + ((releaseCandidate == null) ? 0 : releaseCandidate.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultSemanticVersion other = (DefaultSemanticVersion) obj;
        if (metadata == null) {
            if (other.metadata != null)
                return false;
        } else if (!metadata.equals(other.metadata))
            return false;
        if (major != other.major)
            return false;
        if (minor != other.minor)
            return false;
        if (patch != other.patch)
            return false;
        if (releaseCandidate == null) {
            if (other.releaseCandidate != null)
                return false;
        } else if (!releaseCandidate.equals(other.releaseCandidate))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "SemanticVersion [major=" + major + ", minor=" + minor
                + ", patch=" + patch + ", releaseCandidate=" + releaseCandidate
                + ", build=" + metadata + "]";
    }

    /**
     * Implementation of the semver.org 2.0.0-rc.2 comparison rules
     */
    @Override
    public int compareTo(SemanticVersion other) {
        return COMPARATOR.compare(this, other);
    }

    private static void checkArgument(boolean condition, String errorMessage) {
        if (!condition) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

}
