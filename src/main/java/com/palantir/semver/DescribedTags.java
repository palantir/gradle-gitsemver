package com.palantir.semver;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class DescribedTags {

    private DescribedTags() {
        // prevents instantiation
    }

    public static TagVersionAndCount resolveLatestTagVersionAndCount(
            Repository repo, TagVersionAndCount curTag, int recur) throws IOException,
            RefNotFoundException, GitAPIException {
        Git git = new Git(repo);
        String described = git.describe().setTarget(curTag.getVersion()).call();
        if (described == null)
            return null;
        TagVersionAndCount describedTag = parseDescribeOutput(described);
        if (!SemanticVersions.isValid(GitRepos.stripVFromVersionString(describedTag.getVersion()))) {
            RevWalk revWalk = new RevWalk(repo);
            RevCommit describedRev = revWalk.parseCommit(repo.resolve(describedTag.getVersion()));
            TagVersionAndCount mostRecentParentTag = new TagVersionAndCount("", Integer.MAX_VALUE);
            for (RevCommit parent : describedRev.getParents()) {
                TagVersionAndCount parentTag = new TagVersionAndCount(parent.name(), -1);
                TagVersionAndCount resolvedParentTag = resolveLatestTagVersionAndCount(repo, parentTag, recur + 1);
                if (resolvedParentTag == null)
                    continue;
                if (resolvedParentTag.getCount() < mostRecentParentTag.getCount()) {
                    mostRecentParentTag = resolvedParentTag;
                }
            }
            if (mostRecentParentTag.getCount() == Integer.MAX_VALUE) {
                return null;
            }
            return mostRecentParentTag;
        } else {
        	if (recur != 0)
        		return new TagVersionAndCount(describedTag.getVersion(), -1);
            return describedTag;
        }
    }

    private static TagVersionAndCount parseDescribeOutput(String describe) {
        String tagOffsetRegex = "^(.+)-([0-9]*)-g[0-9a-f]+$";
        // Form 1: <tag>-<count>-<hash>
        Pattern tagOffsetPattern = Pattern.compile(tagOffsetRegex);
        Matcher m = tagOffsetPattern.matcher(describe);
        if (m.matches()) {
            return new TagVersionAndCount(m.group(1), Integer.parseInt(m.group(2)));
        }

        // Form 2:
        return new TagVersionAndCount(describe, 0);
    }

    private static TagVersionAndCount fixCommitCount(TagVersionAndCount resolved, Repository repo) throws RefNotFoundException, GitAPIException {
        Git git = new Git(repo);
        ObjectId target, head;
        LogCommand logCommand;
        try {
            target = repo.getRef(resolved.getVersion()).getPeeledObjectId();
            logCommand = git.log();
            logCommand.add(target);
        } catch (IOException e) {
            throw new SemverGitflowPlugin.VersionApplicationException(e);
        }
        int count = 0;
        for (RevCommit commit : logCommand.call()) {
            count ++;
        }
        return new TagVersionAndCount(resolved.getVersion(), count);
    }

    public static TagVersionAndCount getLatestTagVersionAndCount(Repository repo)
            throws IOException, RefNotFoundException, GitAPIException {
    	TagVersionAndCount tac = resolveLatestTagVersionAndCount(repo, new TagVersionAndCount("HEAD", 0), 0);
    	if (tac.getCount() == -1)
    		return fixCommitCount(tac, repo);
    	return tac;
    }

}
