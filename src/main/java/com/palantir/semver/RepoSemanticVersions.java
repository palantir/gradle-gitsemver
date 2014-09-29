package com.palantir.semver;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public class RepoSemanticVersions {

    private RepoSemanticVersions() {
        // prevents instantiation
    }

    public static Repository getRepo(String repoLocation)
           throws NoWorkTreeException, IOException, GitAPIException {
        Repository repo;
        try {
            repo = new FileRepositoryBuilder().readEnvironment()
                    .findGitDir(new File(repoLocation)).build();
        } catch (IllegalArgumentException iae) {
            throw new SemverGitflowPlugin.VersionApplicationException(
                    "Project is not in a Git repository. Cannot use semver versioning in a non repository.",
                    iae);
        }
        return repo;
    }

    public static String getRepoVersion(String repoLocation, Integer buildNumber)
            throws NoWorkTreeException, IOException, GitAPIException {
        return getRepoVersion(repoLocation, buildNumber, null);
    }

    public static String getRepoVersion(String repoLocation, Integer buildNumber, String prefix)
        throws NoWorkTreeException, IOException, GitAPIException {
        Repository repo = getRepo(repoLocation);

        TagBasedVersionFactory versionFactory;
        if (prefix == null)
            versionFactory = new TagBasedVersionFactory();
        else
            versionFactory = new TagBasedVersionFactory(prefix);
        return versionFactory.createVersion(repo, buildNumber);
    }
}
