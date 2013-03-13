package com.palantir.gradle.gitsemver

import org.eclipse.jgit.api.*
import org.eclipse.jgit.lib.*
import org.eclipse.jgit.revwalk.*
import org.eclipse.jgit.storage.file.*
import org.gradle.api.*
import org.jruby.embed.*
import org.slf4j.LoggerFactory


class GitSemVerPlugin implements Plugin<Project> {

    private static def logger = LoggerFactory.getLogger(GitSemVerPlugin.class)

    public static Map<ObjectId, String> collectTags(Repository r) {
        Map<ObjectId, String> map = new HashMap<ObjectId, String>()
        Map<String, Ref> refs = r.getTags()

        for(Map.Entry<String, Ref> tag : refs.entrySet()) {
            ObjectId tagcommit = tag.getValue().getObjectId()
            Ref ref = r.peel(tag.getValue())
            if (ref.getPeeledObjectId() == null) {
                map.put(ref.getObjectId(), tag.getKey())
            } else {
                map.put(ref.getPeeledObjectId(), tag.getKey())
            }
        }

        return map
    }

    public static List<String> tagsFromHead(Repository repo) {
        List<String> foundTags = new ArrayList()

        Map<ObjectId, String> tags = collectTags(repo)

        RevWalk walk = new RevWalk(repo)
        walk.markStart(walk.parseAny(headObjectId(repo)))

        RevCommit c

        while (c = walk.next()) {
            String tagName = tags.get(c.getId())

            if (tagName != null) {
                foundTags.add(tagName)
            }
        }

        logger.debug("tagsFromHead(${repo}): ${foundTags}")

        return foundTags
    }

	public static ObjectId headObjectId(FileRepository repo) {
		ObjectId headObjectId = repo.getAllRefs().get("HEAD").getObjectId()
		return headObjectId
	}

    public static ObjectId objectIdForTag(Repository repo, String tag) {
        Ref ref = repo.getTags().get(tag)
        repo.peel(ref)
        if (ref.getPeeledObjectId() == null) {
            return ref.getObjectId()
        } else {
            return ref.getPeeledObjectId()
        }
    }

    public static boolean isDirty(Repository repo) {
        Git git = new Git(repo)
        Status status = git.status().call()

        return !(status.getAdded().isEmpty() //
            && status.getChanged().isEmpty() //
            && status.getRemoved().isEmpty() //
            && status.getMissing().isEmpty() //
            && status.getModified().isEmpty() //
            && status.getConflicting().isEmpty());
    }

    private static String describeHeadRegarding(Repository repo, String tag) {
        final ObjectId objectId = objectIdForTag(repo, tag)

        final ObjectId headOid = headObjectId(repo);

        RevWalk walk = new RevWalk(repo)
        walk.markStart(walk.parseCommit(headOid))
        walk.sort(RevSort.TOPO)



        int commitCount = 0
        for (RevCommit c = walk.next(); c != null; c = walk.next()) {
            if (c.getId().equals(objectId)) {
                walk.markUninteresting(c)
            } else {
                commitCount++
            }
        }

        if (commitCount > 0) {
            return "${tag}+${commitCount}.g${headOid.abbreviate(7).name()}${isDirty(repo) ? '.dirty' : ''}"
        } else {
            return tag
        }
    }

    private static String largestTag(List<String> foundTags) {
        ScriptingContainer container = new ScriptingContainer()
        container.put("possible_tags", foundTags)
        def stream = GitSemVerPlugin.class.getClassLoader().getResource('calcversion.rb').openStream()

        return container.runScriptlet(stream, 'calcversion.rb')
    }

    void apply(Project project) {
        FileRepository repo = new FileRepository(project.projectDir.absolutePath + "/.git")

        def theTag = largestTag(tagsFromHead(repo))
        def described = describeHeadRegarding(repo, theTag)
        project.version = described
        println project.version
    }


}