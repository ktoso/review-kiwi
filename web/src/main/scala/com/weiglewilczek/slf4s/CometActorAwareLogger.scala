package com.weiglewilczek.slf4s

import com.weiglewilczek.slf4s._
import org.slf4j.{Logger => Slf4jLogger, spi, LoggerFactory}
import org.slf4j.spi.{LocationAwareLogger => Slf4jLocationAwareLogger}
import net.liftweb.http.CometActor

object CometActorAwareLogger {

  def apply[T](clazz: Class[_], comet: Option[CometActor], msg: (String) => T): CometActorAwareLogger[T] =
    new CometActorAwareLogger(clazz, comet, msg)

}

class CometActorAwareLogger[T](clazz: Class[_], comet: Option[CometActor], asMessage: (String) => T) extends Logger {

  protected val slf4jLogger = LoggerFactory getLogger clazz

  def notifyComet(level: String, msg: => String) {
    comet foreach { _ ! asMessage(level + ": " + msg) }
  }

  /**
   * Log a message with ERROR level.
   * @param msg The message to be logged
   */
  override def error(msg: => String) {
    if (slf4jLogger.isErrorEnabled) {
      notifyComet("ERROR", msg)
      slf4jLogger.error(msg)
    }
  }

  /**
   * Log a message with ERROR level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  override def error(msg: => String, t: Throwable) {
    if (slf4jLogger.isErrorEnabled) {
      notifyComet("ERROR", msg)
      slf4jLogger.error(msg, t)
    }
  }

  /**
   * Log a message with WARN level.
   * @param msg The message to be logged
   */
  override def warn(msg: => String) {
    if (slf4jLogger.isWarnEnabled) {
      notifyComet("WARN", msg)
      slf4jLogger.warn(msg)
    }
  }

  /**
   * Log a message with WARN level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  override def warn(msg: => String, t: Throwable) {
    if (slf4jLogger.isWarnEnabled) {
      notifyComet("WARN", msg)
      slf4jLogger.warn(msg, t)
    }
  }

  /**
   * Log a message with INFO level.
   * @param msg The message to be logged
   */
  override def info(msg: => String) {
    if (slf4jLogger.isInfoEnabled) {
      notifyComet("INFO", msg)
      slf4jLogger.info(msg)
    }
  }

  /**
   * Log a message with INFO level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  override def info(msg: => String, t: Throwable) {
    if (slf4jLogger.isInfoEnabled) {
      notifyComet("WARN", msg)
      slf4jLogger.info(msg, t)
    }
  }

  /**
   * Log a message with DEBUG level.
   * @param msg The message to be logged
   */
  override def debug(msg: => String) {
    if (slf4jLogger.isDebugEnabled) {
      notifyComet("DEBUG", msg)
      slf4jLogger.debug(msg)
    }
  }

  /**
   * Log a message with DEBUG level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  override def debug(msg: => String, t: Throwable) {
    if (slf4jLogger.isDebugEnabled) {
      notifyComet("DEBUG", msg)
      slf4jLogger.debug(msg, t)
    }
  }

  /**
   * Log a message with TRACE level.
   * @param msg The message to be logged
   */
  override def trace(msg: => String) {
    if (slf4jLogger.isTraceEnabled) {
      notifyComet("WARN", msg)
      slf4jLogger.trace(msg)
    }
  }

  /**
   * Log a message with TRACE level.
   * @param msg The message to be logged
   * @param t The Throwable to be logged
   */
  override def trace(msg: => String, t: Throwable) {
    if (slf4jLogger.isTraceEnabled) {
      notifyComet("WARN", msg)
      slf4jLogger.trace(msg, t)
    }
  }
}