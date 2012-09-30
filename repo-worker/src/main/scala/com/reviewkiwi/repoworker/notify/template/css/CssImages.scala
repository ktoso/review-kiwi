package com.reviewkiwi.repoworker.notify.template.css

import com.reviewkiwi.common.crypto.Base64
import org.apache.commons.fileupload.util.Streams
import org.apache.commons.io.FilenameUtils

object CssImages {

  lazy val h2JavaFileStyle = fileImage("/images/page_white_cup.png")
  lazy val h2RubyFileStyle = fileImage("/images/page_white_ruby.png")
  lazy val h2MarkdownFileStyle = fileImage("/images/page_white_text.png")
  lazy val h2CodeFileStyle = fileImage("/images/page_white_code.png")

  def fileImageForFile(file: String) = FilenameUtils.getExtension(file) match {
    case "java" => h2JavaFileStyle
    case "groovy" => h2JavaFileStyle
    case "scala" => h2JavaFileStyle
    case "rb" | "irb" => h2RubyFileStyle
    case "md" => h2MarkdownFileStyle
    case _ => h2CodeFileStyle
  }

  private def fileImage(imageFileLocation: String): String = {
    val imageSrc = "http://review.kiwi.project13.pl/images/%s".format(imageFileLocation.replace("/images/", "/icons/"))
    val style = <img src={imageSrc} width="22" height="22" style="padding: 2px"/>

    style.toString()
  }

}

