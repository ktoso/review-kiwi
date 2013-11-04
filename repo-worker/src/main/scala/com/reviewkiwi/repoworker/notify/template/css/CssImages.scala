package com.reviewkiwi.repoworker.notify.template.css

import org.apache.commons.io._


object CssImages {

  lazy val h2JavaFileStyle = fileImage("/images/page_white_cup.png")
  lazy val h2ScalaFileStyle = fileImage("/images/page_white_scala.png")
  lazy val h2RubyFileStyle = fileImage("/images/page_white_ruby.png")
  lazy val h2MarkdownFileStyle = fileImage("/images/page_white_text.png")
  lazy val h2MustacheFileStyle = fileImage("/images/page_white_mustache.png")
  lazy val h2CodeFileStyle = fileImage("/images/page_white_code.png")
  lazy val h2ImageFileStyle = fileImage("/images/image.png")

  def fileImageForFile(file: String) = FilenameUtils.getExtension(file) match {
    case "scala" | "sbt" => h2ScalaFileStyle
    case "groovy" => h2JavaFileStyle
    case "java" => h2JavaFileStyle
    case "rb" | "irb" => h2RubyFileStyle
    case "mustache" => h2MustacheFileStyle
    case "md" => h2MarkdownFileStyle
    case "jpg" | "png" | "gif" => h2ImageFileStyle
    case _ => h2CodeFileStyle
  }

  private def fileImage(imageFileLocation: String): String = {
    val imageSrc = "http://review.kiwi.project13.pl/images/%s".format(imageFileLocation.replace("/images/", "/icons/"))
    val style = <img src={imageSrc} width="22" height="22" style="padding: 2px"/>

    style.toString()
  }

}

