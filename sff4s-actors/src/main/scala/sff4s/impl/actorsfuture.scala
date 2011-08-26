package sff4s.impl

import sff4s._

object ActorsFuture extends Futures {
  def futureEither[A](result: => Either[Throwable, A]): Future[A] =
    new WrappedActorsFuture(scala.actors.Futures future { result })
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
      case None => Left(new TimeoutException(timeoutInMsec))
    }
    
  def isDefined = underlying.isSet
}
