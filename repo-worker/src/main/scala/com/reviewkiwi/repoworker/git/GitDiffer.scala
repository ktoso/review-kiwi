package com.reviewkiwi.repoworker.git

import org.eclipse.jgit.api.Git
import com.weiglewilczek.slf4s.Logging
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.treewalk.{AbstractTreeIterator, CanonicalTreeParser}
import org.eclipse.jgit.diff.{DiffFormatter, DiffConfig, DiffAlgorithm, DiffEntry}
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk
import collection.JavaConverters._
import org.eclipse.jgit.lib.{ObjectId, Repository}
import com.reviewkiwi.common.git.GitWalks
import java.io.ByteArrayOutputStream

class GitDiffer extends Logging
  with GitWalks {

  def diffWithParent(git: Git, c: RevCommit): List[DiffEntry] = {
    implicit val repo = git.getRepository

    val (commit, parent) = withWalk(depth =  2) { walk =>
      val commit = walk.parseCommit(c)
      val parent = walk.parseCommit(commit.getParent(0))
      (commit, parent)
    }

    val oldTreeIter = getTreeIterator(repo, parent)
    val newTreeIter = getTreeIterator(repo, commit)

    git.diff()
      .setOutputStream(System.out)
      .setOldTree(oldTreeIter)
      .setNewTree(newTreeIter)
      .call().asScala.toList
  }

  def getTreeIterator(repo: Repository, name: String): AbstractTreeIterator = {
    val id = repo.resolve(name)
    if (id == null)
      throw new IllegalArgumentException(name)
    else
      getTreeIterator(repo, id)
  }

  def getTreeIterator(repo: Repository, id: ObjectId): AbstractTreeIterator = {
    val parser = new CanonicalTreeParser()
    val reader = repo.newObjectReader()

    try {
      parser.reset(reader, new RevWalk(repo, 3).parseTree(id))
      parser
    } finally {
      reader.release()
    }
  }

}
