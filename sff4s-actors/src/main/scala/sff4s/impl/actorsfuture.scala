package sff4s.impl

import sff4s._

object ActorsFuture extends Futures {
  implicit def toFuture[A](underlying: scala.actors.Future[A]): Future[A] =
    future { underlying() }
  
  def futureEither[A](result: => Either[Throwable, A]): Future[A] =
    new WrappedActorsFuture(scala.actors.Futures future {
      try {
        result
      } catch {
        case e: Throwable => Left(e)
      }      
    })
}

class WrappedActorsFuture[A](val underlying: scala.actors.Future[Either[Throwable, A]]) extends Future[A] {
  val factory = ActorsFuture
  
  def get: Either[Throwable, A] =
    try {
      underlying.apply()
    } catch {
      case e: Throwable => Left(e)
    }
    
  def get(timeoutInMsec: Long): Either[Throwable, A] =
    scala.actors.Futures.awaitAll(timeoutInMsec, underlying).head match {
      case Some(value) =>
        try {
          value.asInstanceOf[Either[Throwable, A]]  
        }
        catch {
          case e: Throwable => Left(e)
        }
      case None => Left(TimeoutException(timeoutInMsec))
    }

  def select[U >: A](other: Future[U]): Future[U] =
    factory.futureEither { scala.actors.Futures.awaitEither(underlying, toNative(other)) }
  
  private def toNative[U](other: Future[U]): scala.actors.Future[Either[Throwable, U]] =
    other match {
      case other: WrappedActorsFuture[_] => other.underlying.asInstanceOf[scala.actors.Future[Either[Throwable, U]]]
      case _ => throw IncompatibleFutureException()
    }
            
  def isDefined = underlying.isSet
}
