package com.reviewkiwi.repoworker.marker

object TodoLineMarker extends InterestingLineMarker {

  val Todo = """(?i).* // ?todo(.*)""".r

  def extract(fileName: String, line: String, lineNumber: Int): Option[InterestingLine] = {
    (Todo findFirstIn line) map { l => InterestingLine(fileName, l, lineNumber, InterestType.ToDo) }
  }
}
