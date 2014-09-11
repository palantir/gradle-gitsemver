package com.palantir.semver;

import org.junit.Assert;
import org.junit.Test;

public class SemanticVersionTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNonNegative() {
        new DefaultSemanticVersion("1.1.-1", 1, 1, -1, null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBasicStringWithExtraDot() {
        String versionString = "1.23.24.";
        SemanticVersions.parse(versionString);
    }

    @Test
    public void testBasicStringWithoutReleaseOrBuild() {
        String versionString = "1.23.24";
        doVersionTest(versionString, 1, 23, 24, null, null);
    }

    @Test
    public void testStringWithOnlyReleaseCandidate() {
        String versionString = "1.23.45-dev.1.2.3";
        doVersionTest(versionString, 1, 23, 45, "dev.1.2.3", null);
    }

    @Test
    public void testStringWithOnlyBuild() {
        String versionString = "1.23.45+build.1.2.3";
        doVersionTest(versionString, 1, 23, 45, null, "build.1.2.3");
    }

    @Test
    public void testStringWithRcAndBuild() {
        String versionString = "0.0.1-alpha.34.2+build.32";
        doVersionTest(versionString, 0, 0, 1, "alpha.34.2", "build.32");
    }

    @Test(expected = NullPointerException.class)
    public void testVersionComparisonNull() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.1.1",
                1,
                1,
                1,
                null,
                null);
        version.compareTo(null);
    }

    @Test
    public void testMajorVersionComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3",
                1,
                2,
                3,
                null,
                null);
        DefaultSemanticVersion majorHigher = new DefaultSemanticVersion(
                "2.0.0",
                2,
                0,
                0,
                null,
                null);
        assertComparison(version, majorHigher);

    }

    @Test
    public void testMinorVersionComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "10.2.3",
                10,
                2,
                3,
                null,
                null);
        DefaultSemanticVersion minorHigher = new DefaultSemanticVersion(
                "10.3.3",
                10,
                3,
                3,
                null,
                null);
        assertComparison(version, minorHigher);
    }

    @Test
    public void testPatchVersionComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "10.2.3",
                10,
                3,
                2,
                null,
                null);
        DefaultSemanticVersion patchHigher = new DefaultSemanticVersion(
                "10.3.3",
                10,
                3,
                3,
                null,
                null);
        assertComparison(version, patchHigher);
    }

    @Test
    public void testReleaseCandidateComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3.abc-35",
                1,
                2,
                3,
                "abc.35",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3.abc-40",
                1,
                2,
                3,
                "abc.40",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testReleaseCandidateStringComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-abc",
                1,
                2,
                3,
                "abc",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-def",
                1,
                2,
                3,
                "def",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testReleaseCandidateDifferentLenthsComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-abc.35.3",
                1,
                2,
                3,
                "abc.35.3",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-abc.35",
                1,
                2,
                3,
                "abc.35",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testReleaseCandidateNumericAndNotComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-35",
                1,
                2,
                3,
                "35",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-abc",
                1,
                2,
                3,
                "abc",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testNullAndNonNullReleaseCandidate() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-abc.35",
                1,
                2,
                3,
                "abc.35",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3",
                1,
                2,
                3,
                null,
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testDifferentIdentifierLengthReleaseCandidate() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-abc.35",
                1,
                2,
                3,
                "abc.35",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-abc",
                1,
                2,
                3,
                "abc",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testKeywordVsNormal() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-epsilong",
                1,
                2,
                3,
                "epsilon",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-alpha",
                1,
                2,
                3,
                "alpha",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testDevVsAlpha() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-dev",
                1,
                2,
                3,
                "dev",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-alpha",
                1,
                2,
                3,
                "alpha",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testDevVsBeta() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-dev",
                1,
                2,
                3,
                "dev",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-beta",
                1,
                2,
                3,
                "beta",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testDevVsRc() {
        DefaultSemanticVersion version = new DefaultSemanticVersion(
                "1.2.3-dev",
                1,
                2,
                3,
                "dev",
                null);
        DefaultSemanticVersion rcHigher = new DefaultSemanticVersion(
                "1.2.3-rc",
                1,
                2,
                3,
                "rc",
                null);
        assertComparison(version, rcHigher);
    }

    @Test
    public void testEqualComparison() {
        DefaultSemanticVersion version = new DefaultSemanticVersion("10.2.3-dev.1.2.3+build.30",
                10,
                2,
                3,
                "dev.1.2.3",
                "build.30");
        DefaultSemanticVersion sameVersion = new DefaultSemanticVersion(
                "10.2.3-dev.1.2.3+build.30",
                10,
                2,
                3,
                "dev.1.2.3",
                "build.30");
        Assert.assertEquals(0, sameVersion.compareTo(version));
        Assert.assertEquals(0, version.compareTo(sameVersion));
    }

    private static void assertComparison(DefaultSemanticVersion lower,
                                  DefaultSemanticVersion higher) {
        Assert.assertTrue(higher.compareTo(lower) > 0);
        Assert.assertTrue(lower.compareTo(higher) < 0);
    }

    private static void doVersionTest(String versionString,
                                      int major,
                                      int minor,
                                      int patch,
                                      String rc,
                                      String build) {
        SemanticVersion version = SemanticVersions.parse(versionString);
        assertVersion(version, major, minor, patch, rc, build);
    }

    private static void assertVersion(SemanticVersion version,
                                      int major,
                                      int minor,
                                      int patch,
                                      String rc,
                                      String build) {
        Assert.assertEquals(major, version.getMajorVersion());
        Assert.assertEquals(minor, version.getMinorVersion());
        Assert.assertEquals(patch, version.getPatchVersion());
        Assert.assertEquals(rc, version.getReleaseCandidate());
        Assert.assertEquals(build, version.getMetadata());
    }
}
