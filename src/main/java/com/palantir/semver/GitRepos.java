package com.palantir.semver;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.GradleException;

public class GitRepos {

    private GitRepos() {
        // prevents instantiation
    }

        public static boolean isDirty(Repository repo) throws NoWorkTreeException,
                GitAPIException {
            Git git = new Git(repo);
        Status status = git.status().call();
        return !status.isClean();
    }

    public static String stripVFromVersionString(String lastTag) {
        if (lastTag.startsWith("v")) {
            return lastTag.substring(1);
        } else {
            return lastTag;
        }
    }

    public static void printJgitStatus(Repository repo){
        Git git;
        Status status;
        try {
            git = new Git(repo);
            status = git.status().call();
        } catch (NoWorkTreeException e) {
            throw new GradleException("Git exception - No Work Tree", e);
        } catch (GitAPIException e) {
            throw new GradleException("Git API Exception", e);
        }
        System.out.println("--------------------------------");
        System.out.println("GIT STATUS: " + (status.isClean() ? "Clean" : "Dirty"));
        for (String added : status.getAdded()) {
            System.out.println("ADDED: " + added);
        }
        for (String changed : status.getChanged()) {
            System.out.println("CHANGED: " + changed);
        }
        for (String modified : status.getModified()) {
            System.out.println("MODIFIED: " + modified);
        }
        for (String conflicting : status.getConflicting()) {
            System.out.println("CONFLICTING: " + conflicting);
        }
        for (String ignored : status.getIgnoredNotInIndex()) {
            System.out.println("IGNOREDNOTININDEX: " + ignored);
        }
        for (String deleted : status.getRemoved()) {
            System.out.println("DELETED: " + deleted);
        }
        System.out.println(status.toString());
    }

    public static String getHeadCommitIdAbbreviation(Repository repo) {
        ObjectId headObjectId = getHeadObjectId(repo);
        return headObjectId.abbreviate(7).name();
    }

    public static ObjectId getHeadObjectId(Repository repo) {
        Ref headRef = repo.getAllRefs().get("HEAD");
        if (headRef == null) {
            throw new SemverGitflowPlugin.VersionApplicationException(
                    "Project is not in a Git repository. Cannot use semver versioning in a non repository.");
        }
        return headRef.getObjectId();
    }

}
