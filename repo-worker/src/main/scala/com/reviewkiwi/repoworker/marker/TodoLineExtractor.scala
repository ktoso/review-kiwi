package com.reviewkiwi.repoworker.marker

import InterestType._

class TodoLineExtractor extends InterestingLineExtractor {


  val Todo = """(?i).*// ?TODO(\w+)""".r

  def extract(fileName: String, line: String, lineNumber: Int): Option[InterestingLine] = {
    (Todo findFirstIn line) map { l => InterestingLine(fileName, l, lineNumber, InterestType.ToDo) }
  }
}
