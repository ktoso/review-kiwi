package com.reviewkiwi.repoworker.marker.scala.rogue

import com.reviewkiwi.repoworker.marker.{InterestType, InterestingLine, InterestingLineMarker}
import scalaz.Scalaz._

object UpsertWithoutReturnNewLineMarker extends InterestingLineMarker {

  def extract(fileName: String, line: String, number: Int) =
    if (line.contains("upsertOne()"))
      InterestingLine(
        fileName, line, number,
        InterestType.Caution,
        """Are you sure you didn't mean <code>upsertOne(returnNew = true)</code>?""".some).some
    else
      None

}
