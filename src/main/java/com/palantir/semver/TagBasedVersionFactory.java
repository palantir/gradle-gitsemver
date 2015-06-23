package com.palantir.semver;

import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;

public class TagBasedVersionFactory {

    private static final String STABLE_VERSION_REGEX = "^[0-9]+\\.[0-9]+\\.[0-9]+$";
    private String prefix;

    public TagBasedVersionFactory() {
        prefix = null;
    }

    public TagBasedVersionFactory(String prefix) {
        this.prefix = prefix;
    }

    public SemverVersion createVersion(Repository repo, Integer buildNumber)
        throws MissingObjectException, IncorrectObjectTypeException, IOException,
                          NoWorkTreeException, GitAPIException {
        return createVersion(repo, buildNumber, false);
    }

    public SemverVersion createTopoVersion(Repository repo, Integer buildNumber)
        throws MissingObjectException, IncorrectObjectTypeException, IOException,
                          NoWorkTreeException, GitAPIException {
        return createVersion(repo, buildNumber, true);
    }

    public SemverVersion createVersion(Repository repo, Integer buildNumber, boolean topo)
        throws MissingObjectException, IncorrectObjectTypeException, IOException,
                          NoWorkTreeException, GitAPIException {
            if (repo == null) {
                throw new SemverGitflowPlugin.VersionApplicationException(
                        "Project is not in a Git repository. Cannot use semver versioning in a non repository.");
            }
            TagVersionAndCount latestTagAndCount;
            if (topo) {
                latestTagAndCount = Tags.getTopoTagVersionAndCount(repo, prefix);
            } else {
                latestTagAndCount = Tags.getLatestTagVersionAndCount(repo, prefix);
            }
            String headCommitAbbreviation = GitRepos.getHeadCommitIdAbbreviation(repo);
            boolean isDirty = GitRepos.isDirty(repo);
            return generateVersion(latestTagAndCount, headCommitAbbreviation, buildNumber, isDirty);
    }

    private SemverVersion generateVersion(TagVersionAndCount latestTagAndCount,
                                   String headCommitAbbreviation,
                                   Integer buildNumber,
                                   boolean isDirty) {
        StringBuilder versionString = new StringBuilder();

        String matchingTag = latestTagAndCount.getVersion();
        String version;
        if (prefix != null) {
            String versionRegex = "^" + prefix + "-v?(.*)$";
            Pattern versionPattern = Pattern.compile(versionRegex);
            Matcher matcher = versionPattern.matcher(matchingTag);
            if (!matcher.matches()) {
                return new SemverVersion("0.0.0", "0.0.0", "000000000000", 0, null, false);
            }
            version = matcher.group(1);
        } else {
            version = matchingTag;
        }
        versionString.append(version);

        if (isVersionStableRelease(latestTagAndCount)) {
            if (isDirty) {
                versionString.append("+dirty");
            }
        } else {
            String firstSeparator = getFirstSeparator(latestTagAndCount);
            versionString.append(firstSeparator + latestTagAndCount.getCount());
            versionString.append("+g");
            versionString.append(headCommitAbbreviation);
            if (buildNumber != null) {
                versionString.append(".b" + buildNumber);
            }
            if (isDirty) {
                versionString.append(".dirty");
            }
        }
        SemverVersion versionObject = new SemverVersion(versionString.toString(), version, headCommitAbbreviation, latestTagAndCount.getCount(),
                buildNumber, isDirty);
        return versionObject;
    }

    private String getFirstSeparator(TagVersionAndCount latestTag) {
        if (isAboveStable(latestTag)) {
            return "-";
        } else {
            return ".";
        }
    }

    private static boolean isAboveStable(TagVersionAndCount latestTagAndCount) {
        return latestTagAndCount.getVersion().matches(STABLE_VERSION_REGEX)
                && (latestTagAndCount.getCount() > 1);
    }

    private static boolean isVersionStableRelease(TagVersionAndCount latestTagAndCount) {
        return latestTagAndCount.getVersion().matches(STABLE_VERSION_REGEX)
                && isTagCountStable(latestTagAndCount.getCount());
    }

    private static boolean isTagCountStable(int count) {
        return count == 0;
    }

}
