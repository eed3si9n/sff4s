package sff4s

trait Futures {
  def future[A](result: => A): Future[A] = futureEither(Right(result))
  def futureEither[A](result: => Either[Throwable, A]): Future[A]
}

object Future {
  val DEFAULT_TIMEOUT: Long = -1
}

/**
 * A computation evaluated asynchronously.
 * Much of the code taken from https://github.com/twitter/util/blob/master/util-core/src/main/scala/com/twitter/util/Future.scala.
 */
abstract class Future[+A] {    
  protected def factory: Futures
  
  /**
   * Block indefinitely, wait for the result of the Future to be available.
   */
  def apply(): A = get.fold(throw _, x => x)
  
  /**
   * Block as long as the given timeoutInMsec if non-negative, otherwise indefinitely.
   */
  def apply(timeoutInMsec: Long): A = get(timeoutInMsec).fold(throw _, x => x)
  
  /**
   * Demands that the result of the future be available, and blocks indefinitly.
   */
  def get: Either[Throwable, A]
     
  /**
   * Demands that the result of the future be available within `timeoutInMsec`.
   * The result is a Right[A] or Left[Throwable] depending upon whether the computation 
   * finished in time.
   */
  def get(timeoutInMsec: Long): Either[Throwable, A]
    
  /**
   * When the computation completes, invoke the given callback function.
   */
  def respond(k: Either[Throwable, A] => Unit): Future[A] =
    factory.futureEither {
      val result = get
      k(result)
      result
    }
  
  /**
   * Invoke the callback only if the Future returns successfully. Useful for Scala for comprehensions.
   * Use onSuccess instead of this method for more readable code.
   */
  def foreach(f: A => Unit) { onSuccess(f) }
  
  /**
   * Returns the given function applied to the value from this Right or returns this if this is a Left.
   */
  def flatMap[B](f: A => Future[B]): Future[B] = factory.futureEither { get fold (Left(_), f(_).get) }
  
  /**
   * Maps the given function to the value from this Right or returns this if this is a Left.
   */
  def map[B](f: A => B): Future[B] = factory.futureEither { get.right map {f} }
  
  /**
    * Converts this to a Left if the predicate does not obtain.
    */
  def filter(p: A => Boolean): Future[A] = factory.futureEither { get fold (Left(_),
    value =>
      if (p(value)) Right(value)
      else Left(PredicateException())) }
      
  /**
   * Invoke the function on the result, if the computation was
   * successful.  Returns a chained Future as in `respond`.
   *
   * @return chained Future
   */
  def onSuccess(f: A => Unit): Future[A] =
    respond {
      case Right(value) => f(value)
      case _ =>
    }
    
  /**
   * Invoke the function on the error, if the computation was
   * unsuccessful.  Returns a chained Future as in `respond`.
   *
   * @return chained Future
   */
  def onFailure(rescueException: Throwable => Unit): Future[A] =
    respond {
      case Left(e) => rescueException(e)
      case _ =>
    }    
  
  /**
   * Is the result of the Future available yet?
   */
  def isDefined: Boolean
}

case class TimeoutException(val timeoutInMsec: Long) extends Exception(timeoutInMsec.toString)
case class PredicateException() extends Exception()
