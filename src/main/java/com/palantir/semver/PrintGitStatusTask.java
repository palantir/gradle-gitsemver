package com.palantir.semver;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public class PrintGitStatusTask extends DefaultTask {

    Project project;

    public PrintGitStatusTask() {
        setGroup(SemverGitflowPlugin.GROUP);
        setDescription("Prints the JGit status of this gitflow repo");
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @TaskAction
    public void printStatus() throws NoWorkTreeException, IOException,
            GitAPIException {
        String repoLocation = project.getProjectDir().getAbsolutePath() + "/.git";
        Repository repo = RepoSemanticVersions.getRepo(repoLocation);
        GitRepos.printJgitStatus(repo);
    }

}
