package com.palantir.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;

public class GitRepos {

    private GitRepos() {
        // prevents instantiation
    }

    public static boolean isDirty(Repository repo) throws NoWorkTreeException,
            GitAPIException {
        Git git = new Git(repo);
        Status status = git.status().call();
        return !(status.getAdded().isEmpty() && status.getChanged().isEmpty()
                && status.getRemoved().isEmpty()
                && status.getMissing().isEmpty()
                && status.getModified().isEmpty() && status.getConflicting().isEmpty());
    }

    public static String stripVFromVersionString(String lastTag) {
        if (lastTag.startsWith("v")) {
            return lastTag.substring(1);
        } else {
            return lastTag;
        }
    }

    public static String getHeadCommitIdAbbreviation(Repository repo) {
        ObjectId headObjectId = getHeadObjectId(repo);
        return headObjectId.abbreviate(7).name();
    }

    public static ObjectId getHeadObjectId(Repository repo) {
        return repo.getAllRefs().get("HEAD").getObjectId();
    }

}
