package com.palantir.semver;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

public class PrintVersionTask extends DefaultTask {

    public PrintVersionTask() {
        setGroup(SemverGitflowPlugin.GROUP);
        setDescription("Prints the semantic version of this gitflow repo");
    }

    @TaskAction
    public void printVersion() throws NoWorkTreeException, IOException,
            GitAPIException {
        System.out.println(getProject().getVersion());
    }


}
