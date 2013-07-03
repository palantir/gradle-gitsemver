package com.palantir.semver;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import java.io.IOException;

public class SemverGitflowPlugin implements Plugin<Project> {

    public static final String GROUP = "Semantic Versioning";
    private static final String BUILD_NUMBER_PROPERTY = "BUILD_NUMBER";
    private static final String SEMVER_PROPERTY = "gitSemVersion";

    @Override
    public void apply(Project project) {
        try {
            String version;
            if (project.getParent() != null && project.getParent().getProperties() != null &&
                    project.getParent().getProperties().get(SEMVER_PROPERTY) != null) {
                version = (String) project.getParent().getProperties().get(SEMVER_PROPERTY);
            } else {
                version = getRepoVersion(project);
            }
            project.setProperty(SEMVER_PROPERTY, version);
            addPrintVersionTask(project);
        } catch (NoWorkTreeException e) {
            throw new VersionApplicationException(e);
        } catch (IOException e) {
            throw new VersionApplicationException(e);
        } catch (GitAPIException e) {
            throw new VersionApplicationException(e);
        }
    }

    private static void addPrintVersionTask(Project project) {
        project.getTasks().create("printVersion", PrintVersionTask.class);
    }

    public static String getRepoVersion(Project project)
            throws NoWorkTreeException, IOException, GitAPIException {
        String repoLocation;
        if (System.getenv("GIT_DIR") != null) {
            repoLocation = System.getenv("GIT_DIR");
        } else {
            repoLocation = project.getRootProject().getProjectDir().getAbsolutePath() + "/.git";
        }
        Integer buildNumber = getBuildNumber();
        return RepoSemanticVersions.getRepoVersion(repoLocation, buildNumber);
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

    }

}
