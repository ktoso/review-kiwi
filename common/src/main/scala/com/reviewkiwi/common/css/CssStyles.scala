package com.reviewkiwi.common.css

object CssStyles {

  val InsertColor = "#DFD"
  val AddedIconColor = "#92A942"
  val CopiedColor = "#E9BF2F"
  val DeletedColor = "#FDD"
  val DeletedIconColor = "red"
  val InfoLineColor = "#EAF2F5"

  val preStyle = """font-family: Consolas, "Liberation Mono", Courier, monospace; padding:0; margin:0;max-width: 600px;"""

  val insertLine = "background-color: " + InsertColor + ";" + preStyle
  val deleteLine = "background-color: " + DeletedColor + ";" + preStyle
  val infoLine = "background-color: " + InfoLineColor + ";" + preStyle
  val normalLine = "background-color: #FFFFFF;" + preStyle

  val h2Style = """background: #fafafa;
                  |background: -moz-linear-gradient(#fafafa, #eaeaea);
                  |background: -webkit-linear-gradient(#fafafa, #eaeaea);
                  |border: 1px solid #d8d8d8;
                  |border-bottom: 0;
                  |color: #555;
                  |font: 14px sans-serif;
                  |overflow: hidden;
                  |padding: 10px 6px;
                  |text-shadow: 0 1px 0 white;
                  |margin: 0;""".stripMargin.replaceAll(" ", "")
}
