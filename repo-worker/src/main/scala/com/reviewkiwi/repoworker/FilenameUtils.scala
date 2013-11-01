package com.reviewkiwi.repoworker.utils

import java.io.File

object FilenameUtils {

  def getFilenameWithoutExtension(filename: String) = {
    if (filename == null) {
      null
    } else {
      val index = indexOfLastSeparator(filename)
      filename.substring(index + 1)
    }
  }

  def indexOfLastSeparator(filename: String) =
    indexOfLast(filename, File.separatorChar)

  def indexOfLastDot(filename: String) =
      indexOfLast(filename, '.')

  def indexOfLast(filename: String, what: Char) = {
    if (filename == null)
      -1
    else {
      filename.lastIndexOf(what)
    }
  }

  def getExtension(filename: String): String = {
    val idx = indexOfLastDot(filename)
    filename.substring(idx, filename.length)
  }

  def getName(filename: String) = {
    val idx = indexOfLastDot(filename)
    filename.substring(0, idx)
  }

  // todo make sure
  def getPath(filename: String) = {
    filename
  }
}

