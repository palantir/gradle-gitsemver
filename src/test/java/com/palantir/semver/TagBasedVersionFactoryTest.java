package com.palantir.semver;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.api.errors.NoMessageException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.api.errors.UnmergedPathsException;
import org.eclipse.jgit.api.errors.WrongRepositoryStateException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.io.Files;

public class TagBasedVersionFactoryTest {

    private enum Dirty {
        YES, NO;
    }

    private static final PersonIdent COMMITTER = new PersonIdent("a", "a@b");
    private static final String DOT = ".";
    private static final String DASH = "-";

    private Repository repo;
    private Git git;
    private TagBasedVersionFactory versionFactory;

    @Before
    public void createVersionFactory() throws IOException, NoHeadException,
            NoMessageException, UnmergedPathsException,
            ConcurrentRefUpdateException, WrongRepositoryStateException,
            GitAPIException {
        repo = createRepository();
        git = initializeGitFlow(repo);
        versionFactory = new TagBasedVersionFactory();
    }

    private static Repository createRepository() throws IOException {
        File repoDir = Files.createTempDir();
        Repository repo = new FileRepository(new File(repoDir, ".git"));
        repo.create();
        return repo;
    }

    private static Git initializeGitFlow(Repository repo)
            throws RefAlreadyExistsException, RefNotFoundException,
            InvalidRefNameException, GitAPIException {
        Git git = new Git(repo);
        git.commit().setCommitter(COMMITTER).setMessage("initial commit").call();
        return git;
    }

    @Test(expected = SemverGitflowPlugin.VersionApplicationException.class)
    public void testNoTags() throws NoWorkTreeException, IOException,
            GitAPIException {
        SemverVersion version = versionFactory.createVersion(repo, null);
    }

    @Test(expected = SemverGitflowPlugin.VersionApplicationException.class)
    public void testNoRepo() throws NoWorkTreeException, IOException,
            GitAPIException {
        SemverVersion version = versionFactory.createVersion(null, null);
    }


    @Test
    public void testHeadPointsAtStable() throws ConcurrentRefUpdateException,
            InvalidTagNameException, NoHeadException, GitAPIException,
            NoWorkTreeException, MissingObjectException,
            IncorrectObjectTypeException, IOException {
        tag("v1.0.0");
        validateStableTag("1.0.0");
    }

    @Test
    public void testHeadPointsAtStableWhenUsingPrefix() throws ConcurrentRefUpdateException,
            InvalidTagNameException, NoHeadException, GitAPIException,
            NoWorkTreeException, MissingObjectException,
            IncorrectObjectTypeException, IOException {
        versionFactory = new TagBasedVersionFactory("myPrefix");
        tag("myPrefix-v1.0.0");
        validateStableTag("1.0.0");
    }

    @Test
    public void testHeadPointsOneAboveStable()
            throws ConcurrentRefUpdateException, InvalidTagNameException,
            NoHeadException, GitAPIException, NoWorkTreeException,
            MissingObjectException, IncorrectObjectTypeException, IOException {
        tag("v1.0.0");
        RevCommit head = makeCommit();
        validateUnstable("1.0.0", 1, head, Dirty.NO, DOT);
    }

    @Test
    public void testHeadPointsAtUnstableTag() throws NoWorkTreeException,
            IOException, GitAPIException {
        RevCommit head = makeCommit();
        tag("v0.1.0-dev");
        validateUnstable("0.1.0-dev", 0, head, Dirty.NO, DOT);
    }

    @Test
    public void testHeadPointsAtCommitAboveStable() throws NoWorkTreeException,
            IOException, GitAPIException {
        tag("1.0.0");
        makeCommit();
        RevCommit commit = makeCommit();
        validateUnstable("1.0.0", 2, commit, Dirty.NO, DASH);
    }

    @Test
    public void testCommitCount() throws NoHeadException, NoMessageException,
            UnmergedPathsException, ConcurrentRefUpdateException,
            WrongRepositoryStateException, GitAPIException,
            NoWorkTreeException, IOException {
        tag("v0.1.1-rc");
        makeCommit();
        makeCommit();
        RevCommit head = makeCommit();
        validateUnstable("0.1.1-rc", 3, head, Dirty.NO, DOT);
    }

    @Test
    public void testVUnnecessary() throws ConcurrentRefUpdateException,
            InvalidTagNameException, NoHeadException, GitAPIException,
            NoWorkTreeException, IOException {
        makeCommit();
        tag("0.1.0");
        validateStableTag("0.1.0");
    }

    @Test
    public void testNonVersionedTagIsSkipped() throws NoWorkTreeException,
            IOException, GitAPIException {
        makeCommit();
        tag("v0.1.0");
        makeCommit();
        RevCommit head = makeCommit();
        tag("hello");
        validateUnstable("0.1.0", 2, head, Dirty.NO, DASH);
    }

    @Test
    public void testStableIsDirty() throws NoFilepatternException, IOException,
            GitAPIException {
        tag("0.1.0");
        dirtyRepo();
        Assert.assertEquals(
                "0.1.0+dirty",
                versionFactory.createVersion(repo, null).toString());
    }

    @Test
    public void testUnstableIsDirty() throws ConcurrentRefUpdateException,
            InvalidTagNameException, NoHeadException, GitAPIException,
            NoWorkTreeException, MissingObjectException,
            IncorrectObjectTypeException, IOException {
        RevCommit commit = makeCommit();
        tag("0.1.0-dev");
        dirtyRepo();
        validateUnstable("0.1.0-dev", 0, commit, Dirty.YES, DOT);
    }

    @Test
    public void testOrdering() throws ConcurrentRefUpdateException,
            InvalidTagNameException, NoHeadException, GitAPIException,
            NoWorkTreeException, MissingObjectException,
            IncorrectObjectTypeException, IOException {
        tag("3.0.0");
        makeCommit();
        RevCommit head = makeCommit();
        tag("1.0.0");
        validateUnstable("3.0.0", 2, head, Dirty.NO, DASH);
    }

    @Test
    public void testTwoTagsSameCommit() throws NoWorkTreeException,
            MissingObjectException, IncorrectObjectTypeException, IOException,
            GitAPIException {
        tag("1.0.0-rc");
        tag("1.0.0");
        validateStableTag("1.0.0");
    }

    private void validateStableTag(String expectedVersion)
            throws NoWorkTreeException, MissingObjectException,
            IncorrectObjectTypeException, IOException, GitAPIException {
        SemverVersion version = versionFactory.createVersion(repo, null);
        Assert.assertEquals(expectedVersion, version.toString());
        SemverVersion versionBuild = versionFactory.createVersion(repo, 123);
        Assert.assertEquals(expectedVersion, versionBuild.toString());
    }

    private void validateUnstable(String expectedVersion,
                                  int commitCount,
                                  RevCommit headCommit,
                                  Dirty dirty,
                                  String firstModifier)
            throws NoWorkTreeException, IOException, GitAPIException {
        String headCommitId = headCommit.getId().abbreviate(7).name();
        String dirtyText = (dirty == Dirty.YES) ? ".dirty" : "";
        String expected = expectedVersion + firstModifier + commitCount + "+g"
                + headCommitId + dirtyText;
        SemverVersion version = versionFactory.createVersion(repo, null);
        Assert.assertEquals(expected, version.toString());
        Integer buildNumber = 123;
        String expectedWithBuildNumber = expectedVersion + firstModifier
                + commitCount + "+g" + headCommitId + ".b" + buildNumber
                + dirtyText;
        Assert.assertEquals(
                expectedWithBuildNumber,
                versionFactory.createVersion(repo, buildNumber).toString());
    }

    private void dirtyRepo() throws IOException, NoFilepatternException,
            GitAPIException {
        new File(repo.getDirectory().getParent(), "hello.txt").createNewFile();
        git.add().addFilepattern("hello.txt").call();
    }

    private Ref tag(String tagName) throws ConcurrentRefUpdateException,
            InvalidTagNameException, NoHeadException, GitAPIException {
        return git.tag().setMessage("blah").setName(tagName).call();
    }

    private RevCommit makeCommit() throws NoHeadException, NoMessageException,
            UnmergedPathsException, ConcurrentRefUpdateException,
            WrongRepositoryStateException, GitAPIException {
        return git.commit().setCommitter(COMMITTER).setMessage("some commit").call();
    }

}
