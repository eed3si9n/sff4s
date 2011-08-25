package sff4s.impl

import sff4s._

object ActorsFuture extends Futures {
  def future[A](result: => A): Future[A] = new WrappedActorsFuture(
    scala.actors.Futures.future(result)
  )
}

class WrappedActorsFuture[A](val underlying: scala.actors.Future[A]) extends Future[A] {
  def isDefined = underlying.isSet
  
  def get(timeoutInMsec: Long): Either[Throwable, A] =
    if (timeoutInMsec < 0)
      try {
        Right(underlying.apply())
      }
      catch {
        case e: Throwable => Left(e)
      }
    else
      try {
        scala.actors.Futures.awaitAll(timeoutInMsec, underlying).head match {
          case Some(value) => Right(value.asInstanceOf[A])  
          case _ => Left(new TimeoutException(timeoutInMsec))
        }
      }
      catch {
        case e: Throwable => Left(e)
      }
}
