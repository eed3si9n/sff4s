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
    
  def get(timeoutInMsec: Long): Either[Throwable, A] = {
    val x = scala.actors.Futures.awaitAll(timeoutInMsec, underlying).head
    x match {
      case Some(value) => value.asInstanceOf[Either[Throwable, A]]
      case None => Left(new TimeoutException(timeoutInMsec))
    }
  }
    
  def isDefined = underlying.isSet
}

class WrappedActorsThrowable[A](val underlying: Throwable) extends Future[A] {
  val factory = ActorsFuture
  def get: Either[Throwable, A] = Left(underlying)
  def get(timeoutInMsec: Long): Either[Throwable, A] = get
  def isDefined = true 
}
