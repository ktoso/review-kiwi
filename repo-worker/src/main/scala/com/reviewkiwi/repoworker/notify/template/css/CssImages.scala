package com.reviewkiwi.repoworker.notify.template.css

import com.reviewkiwi.common.crypto.Base64
import org.apache.commons.fileupload.util.Streams

object CssImages {

  lazy val h2JavaFileStyle = h2StyleWithImage("/images/page_white_cup.png")
  lazy val h2RubyFileStyle = h2StyleWithImage("/images/page_white_ruby.png")
  lazy val h2MarkdownFileStyle = h2StyleWithImage("/images/page_white_text.png")
  lazy val h2CodeFileStyle = h2StyleWithImage("/images/page_white_code.png")

  def h2StyleForExtension(ext: String) = ext match {
    case "java" => h2JavaFileStyle
    case "groovy" => h2JavaFileStyle
    case "scala" => h2JavaFileStyle
    case "rb" | "irb" => h2RubyFileStyle
    case "md" => h2MarkdownFileStyle
    case _ => h2CodeFileStyle
  }

  private def h2StyleWithImage(imageFileLocation: String): String = {
    val imageBytes = bytesOf(imageFileLocation)
    val imageBase64 = fileToBase64(imageBytes)

    """background: url(data:image/png;base64,%s) no-repeat left center; padding-left: 22px;""".format(imageBase64)
  }

  private def bytesOf(s: String) = {
    val stream = getClass.getResourceAsStream(s)
    Streams.asString(stream).getBytes
  }

  private def fileToBase64(bytes: Seq[Byte]): String = Base64.encode(bytes).replaceAll("\n", "")

}

