package com.palantir.semver;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;

public class TagBasedVersionFactory {

    private static final String STABLE_VERSION_REGEX = "^[0-9]+\\.[0-9]+\\.[0-9]+$";

    public String createVersion(Repository repo, Integer buildNumber)
            throws MissingObjectException, IncorrectObjectTypeException,
            IOException, NoWorkTreeException, GitAPIException {
        TagVersionAndCount latestTagAndCount = Tags.getLatestTagVersionAndCount(repo);
        String headCommitAbbreviation = GitRepos.getHeadCommitIdAbbreviation(repo);
        boolean isDirty = GitRepos.isDirty(repo);
        return generateVersion(
                latestTagAndCount,
                headCommitAbbreviation,
                buildNumber,
                isDirty);
    }

    private String generateVersion(TagVersionAndCount latestTagAndCount,
                                   String headCommitAbbreviation,
                                   Integer buildNumber,
                                   boolean isDirty) {
        StringBuilder versionString = new StringBuilder();
        versionString.append(latestTagAndCount.getVersion());
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
        return versionString.toString();
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
