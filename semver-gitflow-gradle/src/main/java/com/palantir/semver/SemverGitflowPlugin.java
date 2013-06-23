package com.palantir.semver;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class SemverGitflowPlugin implements Plugin<Project> {

    public static final String GROUP = "Semantic Versioning";
    private static final String BUILD_NUMBER_PROPERTY = "BUILD_NUMBER";

    @Override
    public void apply(Project project) {
        try {
            String version = getRepoVersion(project);
            project.setVersion(version);
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
        String repoLocation = project.getProjectDir().getAbsolutePath()
                + "/.git";
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
