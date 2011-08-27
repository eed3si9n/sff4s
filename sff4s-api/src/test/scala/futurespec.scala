import org.specs2._

import sff4s.{Future, Futures, TimeoutException}

trait FutureSpec extends Specification {
  def isFuture(v: => Future[Int], n: Int) =
    "evaluate to %d eventually".format(n)                                     ! F(v, n).e1
      
  case class F(v: Future[Int], n: Int) {
    def e1 = v() must be_==(n)
  }
  
  def isAsync(v: => Future[Int]) =
    "not be defined right away"                                               ! A(v).e1^  
    "be defined eventually"                                                   ! A(v).e2^
    "throw a TimeoutException for 50 msec wait"                               ! A(v).e3
    
  case class A(v: Future[Int]) {
    def e1 = v.isDefined must beFalse
    def e2 = {
      val f = v
      f()
      f.isDefined must beTrue
    }
    def e3 = v(50) must throwA[TimeoutException]
  }
  
  def factory: Futures
  def future: Future[Int] =
    factory future {
      Thread.sleep(500)
      1
    }
  def mapped =
    future map { result =>
      Thread.sleep(100)
      result + 1
    }
}
