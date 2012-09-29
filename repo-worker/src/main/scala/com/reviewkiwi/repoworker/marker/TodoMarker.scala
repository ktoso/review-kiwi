package com.reviewkiwi.repoworker.marker

class TodoMarker extends CStyleCommentMarker {
  val ToDoComment = """(?i)//.+TODO(.+)""".r

  def mark(line: String, above: String) = line match {
    case ToDoComment(message) => Some(line)
    case _ => None
  }
}
