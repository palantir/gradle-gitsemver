package com.palantir.semver

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.testfixtures.ProjectBuilder;

class SemverConventionTest {

  Project project;

  @Before
  public void setupProjectWithPublishing() {
    project = ProjectBuilder.builder().withName("TestProject").build()
    project.apply plugin: SemverGitflowPlugin
  }

  @Test
  public void testPluignsApplied() {
    assertTrue(project.getPlugins().findPlugin(SemverGitflowPlugin) != null)
  }

  // If semverVersion throws the correct exception, it indicates that the addition
  // of the convention worked, and semverVersion is a valid method in Project
  @Test(expected = SemverGitflowPlugin.VersionApplicationException.class)
  public void testConvention() {
    project.semverVersion()
  }

  @Test(expected = SemverGitflowPlugin.VersionApplicationException.class)
  public void testPrefixConvention() {
    project.prefixSemverVersion("testprefix")
  }

  @Test(expected = IllegalArgumentException.class)
  public void testPrefixConventionNoArgument() {
    project.prefixSemverVersion()
  }
}
