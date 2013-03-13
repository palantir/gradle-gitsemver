#!/usr/bin/env ruby

#    Copyright 2013 Palantir Technologies
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.

TEST = ENV['test'] || false

class Version
  attr_reader :ver, :qual, :build
  include Comparable

  SPECIAL_WORDS = {"dev" => 100001, "alpha" => 100002, "beta" => 100003, "rc" => 100004}

  def initialize(str)
    m = /^v?(\d+\.\d+\.\d+)(\-([0-9A-Za-z\-\.]+?))?(\+([0-9A-Za-z\-\.]+?))?$/.match(str.strip)
    raise "Unable to parse version '#{str}'" if m.nil?
    @ver, @qual, @build = [1, 3, 5].map {|i| m[i] && m[i].split('.')}.each do |c|
      c.map! do |val|
        if "#{val.to_i}" == val
          val.to_i
        else
          val
        end
      end if c
    end
  end

  def <=>(anOther)
    val = compare_parts(ver, anOther.ver, false)
    return val if val != 0

    val = compare_parts(qual, anOther.qual, false)
    return val if val != 0

    val = compare_parts(build, anOther.build, true)
    return val
  end

  def to_s
    r = ver.join('.')
    r += '-' + qual.join('.') if (qual)
    r += '+' + build.join('.') if (build)
    r
  end


  def self.compare(a, b)
    Version.new(a) <=> Version.new(b)
  end

  private
  def compare_parts(a, b, nil_is_small)
    nil_mod = nil_is_small ? 1 : -1
    return 0 if a == b
    return nil_mod * -1 if a == nil
    return nil_mod * 1 if b == nil

    a.each_with_index do |elem, i|
      a_val = SPECIAL_WORDS[a[i]] || a[i]
      b_val = SPECIAL_WORDS[b[i]] || b[i]
      return 1 if b_val.nil?
      return -1 if Fixnum === a_val && String === b_val
      return 1 if String === a_val && Fixnum === b_val
      val = a_val <=> b_val
      return val unless val == 0
    end

    return -1 if b.size > a.size
    return 0
  end
end

if TEST then
  require 'minitest/autorun'

  class TestCompareVersion < MiniTest::Unit::TestCase
    def assert_smaller(a, b)
      assert_equal(-1, Version.compare(a, b), "#{a} should be smaller than #{b}")
      assert_equal(1, Version.compare(b, a), "#{a} should be smaller than #{b}")
    end

    def test_parsing
      v = Version.new("1.0.0-foo.1+bar2")
      assert_equal [1, 0, 0], v.ver
      assert_equal ["foo", 1], v.qual
      assert_equal ["bar2"], v.build

      assert_raises(RuntimeError){Version.new("1.0")}
    end

    def test_equality
      assert_equal Version.new("1.0.0"), Version.new("v1.0.0")
    end

    def test_versions
      assert_smaller("1.0.0", "2.0.0")
    end

    def test_qualifiers
      assert_smaller("1.0.0-foo", "1.0.0")
    end

    def test_build_data
      assert_smaller("1.0.0", "1.0.0+foo")
    end

    def test_special_words
      assert_smaller("1.0.0-dev", "1.0.0-alpha.1")
    end

    def test_it_all
      assert_smaller("1.0.0-alpha", "1.0.0-alpha.1")
      assert_smaller("1.3.7+build", "1.3.7+build.2.b8f12d7")
      assert_smaller("1.0.0-alpha.1", "1.0.0-beta.2")
      assert_smaller("1.0.0-beta.2", "1.0.0-beta.11")

      ordered_correctly = ["1.0.0-alpha", "1.0.0-alpha.1", "1.0.0-beta.2", "1.0.0-beta.11", "1.0.0-rc.1", "1.0.0-rc.1+build.1", "1.0.0", "1.0.0+0.3.7", "1.3.7+build", "1.3.7+build.2.b8f12d7", "1.3.7+build.11.e0f985a"]
      shuffled = ordered_correctly.sort {rand}
      sorted = shuffled.sort {|a,b| Version.new(a) <=> Version.new(b)}
      assert_equal ordered_correctly, sorted
    end
  end
end

#grepped_tags = `git log --oneline --decorate=short | grep -E '\\w \\((HEAD, )?tag:'`
#raise grepped_tags

tags = possible_tags
tags.sort! {|a,b| Version.new(a) <=> Version.new(b)}
tag = tags[tags.size() - 1].to_s

raise "There are no tags" if tags.size == 0

tag

#described = `git describe --tags --match #{tag} --dirty`.chomp
#aise "Something bad happened with describe: #{described}. Tags: #{tags}. cwd: #{Dir.pwd}" unless described.start_with? tag

#end_part = described[tag.length..-1]
#end_part = end_part[1..-1] if end_part.start_with? '-'
#end_part = end_part.gsub('-', '.')
#end_part = "+#{end_part}" if end_part.length > 0

#sem_ver = Version.new("#{tag}#{end_part}").to_s

#puts sem_ver

