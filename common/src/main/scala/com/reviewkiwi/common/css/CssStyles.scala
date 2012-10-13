package com.reviewkiwi.common.css

object CssStyles {

  val NormalBackgroundColor = "#FFF"
  val InsertBackgroundColor = "#DFD"
  val DeleteBackgroundColor = "#FDD"
  val InfoBackgroundColor = "#EAF2F5"

  val InsertIconColor = "#8EC340"
  val CopiedColor = "#E9BF2F"
  val DeletedIconColor = "#A43B00"

  val preStyle = """font-family: Consolas, "Liberation Mono", Courier, monospace; max-width: 100%; width: 100%; padding:0; margin:0;"""

  val NormalBackgroundStyle = "background-color: " + "#FFFFFF"
  val InsertBackgroundStyle = "background-color: " + InsertBackgroundColor
  val DeleteBackgroundStyle = "background-color: " + DeleteBackgroundColor
  val InfoBackgroundStyle = "background-color: " + InfoBackgroundColor

  val insertLine = InsertBackgroundStyle + ";" + preStyle
  val deleteLine =  DeleteBackgroundStyle + ";" + preStyle
  val infoLine = InfoBackgroundStyle + ";" + preStyle
  val normalLine = NormalBackgroundStyle + ";" + preStyle

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
