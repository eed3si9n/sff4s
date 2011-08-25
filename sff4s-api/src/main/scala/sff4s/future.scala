package sff4s

trait Futures {
  def future[A](result: => A): Future[A]
}

object Future {
  val DEFAULT_TIMEOUT: Long = -1
}

/**
 * A computation evaluated asynchronously.
 */
abstract class Future[+A] {
  /**
   * Block indefinitely, wait for the result of the Future to be available.
   */
  def apply(): A = apply(Future.DEFAULT_TIMEOUT)
  
  /**
   * Block as long as the given timeoutInMsec if non-negative, otherwise indefinitely.
   */
  def apply(timeoutInMsec: Long): A = get(timeoutInMsec) match {
    case Right(value) => value
    case Left(e) => throw e
  }
  
  /**
   * Demands that the result of the future be available within `timeoutInMsec`.
   * The result is a Right[A] or Left[Throwable] depending upon whether the computation 
   * finished in time.
   */
  def get(timeoutInMsec: Long): Either[Throwable, A]  
  
  /**
   * Is the result of the Future available yet?
   */
  def isDefined: Boolean
}

class TimeoutException(val timeoutInMsec: Long) extends Exception(timeoutInMsec.toString) {
}
