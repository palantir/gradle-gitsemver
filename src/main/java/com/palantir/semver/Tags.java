package com.palantir.semver;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevSort;
import org.eclipse.jgit.revwalk.RevWalk;

public class Tags {

    private Tags() {
        // prevents instantiation
    }

    public static TagVersionAndCount getLatestTagVersionAndCount(Repository repo, String prefix)
            throws MissingObjectException, IncorrectObjectTypeException, IOException {
        TagAndVersion latestTag = getLatestTag(repo, prefix);
        if (latestTag == null) {
            throw new SemverGitflowPlugin.VersionApplicationException(
                    "Cannot find any matching tags in history. You must have tags of form v0.1.2 in order to use semver");
        } else {
            int count = getNumberOfCommitsSinceTag(repo, latestTag.tag);
            return new TagVersionAndCount(latestTag.version.getOriginalVersion(), count);
        }
    }

    private static int getNumberOfCommitsSinceTag(Repository repo, String lastTag)
            throws MissingObjectException, IncorrectObjectTypeException, IOException {
        try {
            ObjectId lastTagObjectId = getObjectIdForTag(repo, lastTag);
            ObjectId headObjectId = GitRepos.getHeadObjectId(repo);
            return getCountBetweenCommits(repo, headObjectId, lastTagObjectId);
        } catch (NullPointerException e) {
            return 0;
        }
    }

    private static TagAndVersion getLatestTag(Repository repo, String prefix) throws MissingObjectException,
            IncorrectObjectTypeException, IOException {
        Map<ObjectId, Set<String>> allTags = getAllTags(repo);
        return findLatestTag(repo, allTags, prefix);
    }

    private static int getCountBetweenCommits(Repository repo,
                                              ObjectId headObjectId,
                                              ObjectId lastTagObjectId)
            throws MissingObjectException, IncorrectObjectTypeException, IOException {
        RevWalk walk = new RevWalk(repo);
        RevCommit startingPoint = walk.parseCommit(headObjectId);
        walk.markStart(startingPoint);
        RevCommit end = walk.lookupCommit(lastTagObjectId);
        walk.sort(RevSort.TOPO);
        int commitCount = 0;
        for (RevCommit c = walk.next(); nonNullOrEnd(end, c); c = walk.next()) {
            commitCount++;
        }
        return commitCount;
    }

    private static boolean nonNullOrEnd(RevCommit end, RevCommit c) {
        return (c != null) && !c.equals(end);
    }

    private static ObjectId getObjectIdForTag(Repository repo, String tag) {
        Ref ref = repo.getTags().get(tag);
        repo.peel(ref);
        if (ref.getPeeledObjectId() == null) {
            return ref.getObjectId();
        } else {
            return ref.getPeeledObjectId();
        }
    }

    private static TagAndVersion findLatestTag(Repository repo, Map<ObjectId, Set<String>> allTags, String prefix)
            throws MissingObjectException, IncorrectObjectTypeException, IOException {

        try {
            RevWalk walk = new RevWalk(repo);
            walk.markStart(walk.parseCommit(GitRepos.getHeadObjectId(repo)));
            return getLatestTagFromWalk(walk, allTags, prefix);
        } catch (NullPointerException e) {
            return new TagAndVersion("0.0.0", new DefaultSemanticVersion(
                    "0.0.0",
                    0,
                    0,
                    0,
                    null,
                    null));
        }
    }

    private static TagAndVersion getLatestTagFromWalk(RevWalk walk, Map<ObjectId, Set<String>> tags, String prefix) {
        List<TagAndVersion> foundTags = findAllTagsOnWalk(walk, tags, prefix);
        if (foundTags.isEmpty()) {
            return null;
        } else {
            Collections.sort(foundTags);
            return foundTags.get(foundTags.size() - 1);
        }
    }

    private static List<TagAndVersion> findAllTagsOnWalk(RevWalk walk,
                                                         Map<ObjectId, Set<String>> tags, String prefix) {
        List<TagAndVersion> foundTags = new LinkedList<TagAndVersion>();
        for (RevCommit commit : walk) {
            ObjectId commitId = commit.getId();
            if (tags.containsKey(commitId)) {
                addTagsToListForCommitId(foundTags, tags, commitId, prefix);
            }
        }
        return foundTags;
    }

    private static void addTagsToListForCommitId(List<TagAndVersion> foundTags,
                                                 Map<ObjectId, Set<String>> tags,
                                                 ObjectId commitId, String prefix) {
        for (String tagName : tags.get(commitId)) {
            String tagVersion = GitRepos.stripVFromVersionString(tagName);
            if (prefix == null) {
                if (SemanticVersions.isValid(tagVersion)) {
                    foundTags.add(new TagAndVersion(tagName, SemanticVersions.parse(tagVersion)));
                }
            } else {
                if (SemanticVersions.isValid(prefix, tagVersion)) {
                    foundTags.add(new TagAndVersion(tagName, SemanticVersions.parse(prefix, tagVersion)));
                }
            }
        }
    }

    private static Map<ObjectId, Set<String>> getAllTags(Repository repo) {
        Map<ObjectId, Set<String>> map = new HashMap<ObjectId, Set<String>>();
        Map<String, Ref> refs = repo.getTags();
        for (Map.Entry<String, Ref> tag : refs.entrySet()) {
            ObjectId idForTag = getIdForTag(repo, tag);
            if (map.containsKey(idForTag)) {
                map.get(idForTag).add(tag.getKey());
            } else {
                Set<String> tags = new HashSet<String>();
                tags.add(tag.getKey());
                map.put(idForTag, tags);
            }
        }
        return map;
    }

    private static ObjectId getIdForTag(Repository repo, Map.Entry<String, Ref> tag) {
        Ref ref = repo.peel(tag.getValue());
        if (ref.getPeeledObjectId() == null) {
            return ref.getObjectId();
        } else {
            return ref.getPeeledObjectId();
        }
    }

    private static class TagAndVersion implements Comparable<TagAndVersion> {

        final String tag;
        final SemanticVersion version;

        TagAndVersion(String tag, SemanticVersion version) {
            this.tag = tag;
            this.version = version;
        }

        @Override
        public int compareTo(TagAndVersion other) {
            return version.compareTo(other.version);
        }
    }

}
