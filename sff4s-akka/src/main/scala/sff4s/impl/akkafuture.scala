package sff4s.impl

import sff4s._
import akka.{dispatch => ad}

object AkkaFuture extends Futures {
  implicit def toFuture[A](underlying: ad.Future[A]): Future[A] =
    new WrappedAkkaFuture(underlying)
  
  override def future[A](result: => A): Future[A] = ad.Future(result)
  
  def futureEither[A](result: => Either[Throwable, A]): Future[A] =
    toFuture(ad.Future(result) flatMap { new ad.AlreadyCompletedFuture(_) })
}

/**
 * https://github.com/jboner/akka/blob/master/akka-actor/src/main/scala/akka/dispatch/Future.scala
 */
class WrappedAkkaFuture[A](val underlying: ad.Future[A]) extends Future[A] {
  import AkkaFuture.toFuture
  
  val factory = AkkaFuture
  
  def get: Either[Throwable, A] = underlying.await.value.get
  
  def get(timeoutInMsec: Long): Either[Throwable, A] =
    try {
      import akka.util.FiniteDuration
      import java.util.concurrent.TimeUnit._
      underlying.await(new FiniteDuration(timeoutInMsec, MILLISECONDS)).value.get
    } catch {
      case e: ad.FutureTimeoutException => Left(TimeoutException(timeoutInMsec))
      case e: Throwable => Left(e)
    }
    
  def select[U >: A](other: Future[U]): Future[U] =
    ad.Futures.firstCompletedOf(Seq(underlying, toNative(other)))
      
  private def toNative[U](other: Future[U]): ad.Future[U] =
    other match {
      case other: WrappedAkkaFuture[_] => other.underlying.asInstanceOf[ad.Future[U]]
      case _ => throw IncompatibleFutureException()
    }
  
  def isDefined = underlying.isCompleted
  
  // short-curcuit to underlying implementations
  override def value = underlying.value
  override def respond(k: Either[Throwable, A] => Unit): Future[A] =
    toFuture(underlying.onComplete { _.value match {
      case Some(either) => k(either)
      case _ =>
    }})
  override def foreach(f: A => Unit) { underlying.foreach(f) }
  override def map[B](f: A => B): Future[B] = underlying.map(f)
  override def filter(p: A => Boolean): Future[A] = underlying.filter(p)
  override def onSuccess(f: A => Unit): Future[A] = 
    toFuture(underlying.onComplete { _.value match {
      case Some(Right(value)) => f(value)
      case _ =>
    }})
  override def onFailure(rescueException: Throwable => Unit): Future[A] =
    toFuture(underlying.onComplete { _.value match {
      case Some(Left(e)) => rescueException(e)
      case _ =>
    }})
}
