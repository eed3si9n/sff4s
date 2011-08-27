package sff4s.impl

import sff4s._
import java.util.{concurrent => juc}

class JucFixedThreadPoolFuture(val nThreads: Int) extends JucFuture(
  juc.Executors.newFixedThreadPool(nThreads))

object JucSingleThreadExecutorFuture extends JucFuture(
  juc.Executors.newSingleThreadExecutor)

class JucFuture(val executor: juc.ExecutorService) extends Futures { self =>
  implicit def toFuture[A](underlying: juc.Future[Either[Throwable, A]]): Future[A] =
    new WrappedJucFuture(underlying)  
  
  def futureEither[A](result: => Either[Throwable, A]): Future[A] =
    executor.submit(new juc.Callable[Either[Throwable, A]] {
      def call: Either[Throwable, A] = result
    })
    
  class WrappedJucFuture[A](val underlying: juc.Future[Either[Throwable, A]]) extends Future[A] {
    val factory = self

    def get: Either[Throwable, A] =
      try {
        underlying.get
      } catch {
        case e: Throwable => Left(e)
      }

    def get(timeoutInMsec: Long): Either[Throwable, A] =
      try {
        underlying.get(timeoutInMsec, juc.TimeUnit.MILLISECONDS)
      }
      catch {
        case e: juc.TimeoutException => Left(TimeoutException(timeoutInMsec))
        case e: Throwable => Left(e)
      }

    def isDefined = underlying.isDone
  }    
}
