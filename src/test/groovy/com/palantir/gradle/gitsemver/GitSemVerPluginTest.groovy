/*
   Copyright 2013 Palantir Technologies

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.palantir.gradle.gitsemver;

import static org.junit.Assert.*

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepository
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class GitSemVerPluginTest {

    private File dir;
    private FileRepository repo
    private Git git

    @Before
    public void init() {
        dir = new File("tmp");
        dir.deleteDir();
        dir.mkdir();
        repo = new FileRepository(new File(dir, ".git"));
        repo.create();
        git = new Git(repo);
        git.commit().setCommitter("foo", "bar").setMessage("initial commit").call();
        git.tag().setMessage("msg").setName("v0.1.0").call();
        git.commit().setCommitter("foo", "bar").setMessage("commit 2").call();
        git.tag().setMessage("msg").setName("v0.0.1").call();
        git.commit().setCommitter("foo", "bar").setMessage("commit 3").call();
    }

    @Test
    public void testThatItWorks() {
        Project project = ProjectBuilder.builder().withProjectDir(dir).build();
        project.apply plugin: 'gitsemver'
        Assert.assertThat(project.version, RegexMatcher.matches("v0\\.1\\.0\\+2\\.g.*"))
    }
}
