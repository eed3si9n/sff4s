package sff4s.impl

import sff4s._
import com.twitter.{util => ctu}

object TwitterUtilFuture extends Futures {
  implicit def toFuture[A](underlying: ctu.Future[A]): Future[A] =
    new WrappedTwitterUtilFuture(underlying)
  
  /**
   * twitter-util's Future blocks!
   * `com.twitter.Future` does not implement concurrency.
   */
  override def future[A](result: => A): Future[A] = ctu.Future(result)
  
  /**
   * twitter-util's Future blocks!
   * `com.twitter.Future` does not implement concurrency.
   */  
  def futureEither[A](result: => Either[Throwable, A]): Future[A] =
    toFuture(ctu.Future(result) flatMap {
      case Left(e) => ctu.Future.exception[A](e)
      case Right(value) => ctu.Future(value)
    })
}

/**
 * https://github.com/twitter/util/blob/master/util-core/src/main/scala/com/twitter/util/Future.scala
 */
class WrappedTwitterUtilFuture[A](val underlying: ctu.Future[A]) extends Future[A] {
  import ctu.{Try, Throw, Return}
  import com.twitter.conversions.time._
  import TwitterUtilFuture.toFuture
  
  val factory = TwitterUtilFuture
  
  def get: Either[Throwable, A] = get(ctu.Future.DEFAULT_TIMEOUT)
  
  def get(timeoutInMsec: Long): Either[Throwable, A] =
    underlying.get(timeoutInMsec.milliseconds) match {
      case Throw(e) =>
        e match {
          case e: ctu.TimeoutException => Left(TimeoutException(timeoutInMsec)) 
          case _ => Left(e)
        }
      case Return(value) => Right(value.asInstanceOf[A])      
    }
    
  def isDefined = underlying.isDefined
  
  // short-curcuit to underlying implementations
  override def apply() = underlying.apply()
  override def apply(timeoutInMsec: Long) = underlying.apply(timeoutInMsec.milliseconds)
  override def respond(k: Either[Throwable, A] => Unit): Future[A] =
    toFuture(underlying.respond {
      case Throw(e) => k(Left(e))
      case Return(value) => k(Right(value))
    })
  override def foreach(f: A => Unit) { underlying.foreach(f) }
  override def map[B](f: A => B): Future[B] = underlying.map(f)
  override def filter(p: A => Boolean): Future[A] = underlying.filter(p)
  override def onSuccess(f: A => Unit): Future[A] = underlying.onSuccess(f)
  override def onFailure(rescueException: Throwable => Unit): Future[A] =
    underlying.onFailure(rescueException)
}
