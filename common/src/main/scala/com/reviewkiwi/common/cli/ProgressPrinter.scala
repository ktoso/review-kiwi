package com.reviewkiwi.common.cli

import com.reviewkiwi.common.util.PercentageNoun

class ProgressPrinter(total: Long) {

  var counter = 0L
  var lastPercent = 0L

  print(0)

  def inc() {
    counter += 1

    val currentPercent = PercentageNoun(counter).percent(total)
    if (currentPercent != lastPercent) {
      print(currentPercent)
      lastPercent = currentPercent
    }
  }

  def print(currentPercent: Long) {
    println(currentPercent + "% (" + counter + "/" + total + ")")
  }

}

object ProgressPrinter {

  /** Warning, calls size on the given collection! (May be a bad idea for lazy collections) */
  def foreach[T](c: Iterable[T])(block: T => Unit) {
    val progress = new ProgressPrinter(c.size)
    c foreach { item => block(item); progress.inc() }
  }
}