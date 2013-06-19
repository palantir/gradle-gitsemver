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
        logger.debug("describeHeadRegarding(repo, '${tag}')")
        final ObjectId objectId = objectIdForTag(repo, tag)

        final ObjectId headOid = headObjectId(repo);

        RevWalk walk = new RevWalk(repo)
        def startingPoint = walk.parseCommit(headOid)
        logger.debug("Starting point: ${startingPoint}")
        walk.markStart(startingPoint)
        walk.markUninteresting(walk.lookupCommit(objectId))
        walk.sort(RevSort.TOPO)




        int commitCount = 0
        for (RevCommit c = walk.next(); c != null; c = walk.next()) {
            logger.debug("Counting " + c.getId().abbreviate(7))
            commitCount++
        }

        if (commitCount > 0) {
            return "${tag}+${commitCount}.g${headOid.abbreviate(7).name()}${isDirty(repo) ? '.dirty' : ''}"
        } else {
            return tag
        }
    }

    private static String largestTag(List<String> foundTags) {
        if (foundTags.size() == 0) {
            throw new IllegalArgumentException("There are no tags!")
        }
        ScriptingContainer container = new ScriptingContainer()
        container.put("possible_tags", foundTags)
        def stream = GitSemVerPlugin.class.getClassLoader().getResource('calcversion.rb').openStream()

        return container.runScriptlet(stream, 'calcversion.rb')
    }
    
    /**
    * Recursive search for the .git directory from the current dir to the parents.
    */
    private File findGitDir(File dir) {
       File gitDir = new File(dir, ".git");
       if (gitDir.exists()) {
          return gitDir;
       } else if (dir.getParentFile() != null ) {
          return findGitDir(dir.getParentFile());
       } else {
       	 return null;
       }
    
    }

    void apply(Project project) {
        File gitDir = findGitDir(project.projectDir);
	if (gitDir == null) {
	   throw new FileNotFoundException("Can't find the .git directory in the projectDir or any parent dir");
	}
        FileRepository repo = new FileRepository(gitDir);	
        def tagsFromHead = tagsFromHead(repo)
        logger.debug("Possible tags: ${tagsFromHead}")
        def theTag = largestTag(tagsFromHead)
        logger.debug("The tag we're shooting for: ${theTag}")
        def described = describeHeadRegarding(repo, theTag)
        project.version = described
        logger.debug("Setting version to: ${project.version}")
    }


}