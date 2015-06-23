package com.palantir.semver;

import groovy.lang.Closure;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.gradle.api.Project;

public class SemverConvention {

    private Project project;

    public SemverConvention(Project project) {
        this.project = project;
    }

    public SemverVersion semverVersion(Closure configureClosure) {
        try {
            return SemverGitflowPlugin.getRepoVersion(project);
        } catch (NoWorkTreeException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        } catch (IOException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        } catch (GitAPIException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        }
    }

    public SemverVersion topoSemverVersion(Closure configureClosure) {
        try {
            return SemverGitflowPlugin.getRepoTopoVersion(project);
        } catch (NoWorkTreeException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        } catch (IOException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        } catch (GitAPIException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        }
    }

    public SemverVersion prefixSemverVersion(String prefix) {
        if (prefix == null || prefix.length() == 0) {
            throw new IllegalArgumentException("No prefix specified for prefixSemverVersion");
        }
        try {
            return SemverGitflowPlugin.getRepoVersion(project, prefix);
        } catch (NoWorkTreeException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        } catch (IOException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        } catch (GitAPIException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        }
    }
}
