package com.palantir.semver;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepository;

public class RepoSemanticVersions {

    private RepoSemanticVersions() {
        // prevents instantiation
    }

    public static String getRepoVersion(String repoLocation, Integer buildNumber)
            throws NoWorkTreeException, IOException, GitAPIException {
        Repository repo = new FileRepository(repoLocation);
        TagBasedVersionFactory versionFactory = new TagBasedVersionFactory();
        return versionFactory.createVersion(repo, buildNumber);
    }
}
