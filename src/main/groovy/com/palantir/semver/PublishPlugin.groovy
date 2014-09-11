package com.palantir.semver

import org.gradle.api.Plugin
import org.gradle.api.Project


public class PublishPlugin implements Plugin<Project> {

	void apply(Project project) {
		project.task("publish") {
			println("hi")
		}
	}
}

