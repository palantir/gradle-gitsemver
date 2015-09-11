package com.palantir.semver;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SemverGitflowPlugin implements Plugin<Project> {

    public static final String GROUP = "Semantic Versioning";
    private static final String BUILD_NUMBER_PROPERTY = "BUILD_NUMBER";
    private String prefix;

    @Override
    public void apply(Project project) {
        try {
            SemverConvention convention = new SemverConvention(project);
            project.getConvention().getPlugins().put("semver", convention);
        } catch (NoWorkTreeException e) {
            throw new VersionApplicationException(e);
        }
    }

    public static SemverVersion getRepoVersion(Project project, String prefix)
            throws NoWorkTreeException, IOException, GitAPIException {
        String repoLocation = project.getProjectDir().getAbsolutePath()
                + "/.git";
        Integer buildNumber = getBuildNumber();
        return RepoSemanticVersions.getRepoVersion(repoLocation, buildNumber, prefix);
    }

    public static SemverVersion getRepoVersion(Project project)
            throws NoWorkTreeException, IOException, GitAPIException {
        String repoLocation = project.getProjectDir().getAbsolutePath()
                + "/.git";
        Integer buildNumber = getBuildNumber();
        return RepoSemanticVersions.getRepoVersion(repoLocation, buildNumber);
    }

    public static SemverVersion getRepoTopoVersion(Project project)
            throws NoWorkTreeException, IOException, GitAPIException {
        String repoLocation = project.getProjectDir().getAbsolutePath()
                + "/.git";
        Integer buildNumber = getBuildNumber();
        return RepoSemanticVersions.getRepoTopoVersion(repoLocation, buildNumber);
    }

    public static SemverVersion getRepoTopoVersion(Project project, String prefix)
            throws NoWorkTreeException, IOException, GitAPIException {
        String repoLocation = project.getProjectDir().getAbsolutePath()
                + "/.git";
        Integer buildNumber = getBuildNumber();
        return RepoSemanticVersions.getRepoTopoVersion(repoLocation, buildNumber, prefix);
    }

    private static Integer getBuildNumber() {
        String buildNumber = System.getenv(BUILD_NUMBER_PROPERTY);
        if (buildNumber == null) {
            return null;
        } else {
            return Integer.parseInt(buildNumber);
        }
    }

    public static class VersionApplicationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        VersionApplicationException(Throwable cause) {
            super("Could not assign version to project", cause);
        }

        VersionApplicationException(String message) {
            super(message);
        }

        VersionApplicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
