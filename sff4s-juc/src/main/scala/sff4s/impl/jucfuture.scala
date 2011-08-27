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
  
  def futureEither[A](result: => Either[Throwable, A]): Future[A] = executor.submit(toCallable(result))
  
  private def toCallable[U](result: => U): juc.Callable[U] = new juc.Callable[U] { def call: U = result }
    
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
      
    def select[U >: A](other: Future[U]): Future[U] = {
      import collection.JavaConversions._
      val tasks: java.util.Collection[juc.Callable[Any]] = List(toCallable(get: Any), toCallable(other.get: Any))
      factory.futureEither { executor.invokeAny(tasks).asInstanceOf[Either[Throwable, U]] }
    }
    
    private def toNative[U](other: Future[U]): juc.Future[Either[Throwable, U]] =
      other match {
        case other: WrappedJucFuture[_] => other.underlying.asInstanceOf[juc.Future[Either[Throwable, U]]]
        case _ => throw IncompatibleFutureException()
      }
      
    def isDefined = underlying.isDone
  }    
}
