package com.reviewkiwi.common.gravatar

import de.bripkens.gravatar.{Gravatar => JGravatar}
import de.bripkens.gravatar.DefaultImage
import de.bripkens.gravatar.Rating

trait Gravatar {
  val g = new JGravatar()
    .setHttps(true)
    .setRating(Rating.PARENTAL_GUIDANCE_SUGGESTED)
    .setStandardDefaultImage(DefaultImage.MYSTERY_MAN)

  def getSmallGravatarUrl(email: String) =
    g.setSize(50).getUrl(email)

}

object Gravatar extends Gravatar
