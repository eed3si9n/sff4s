import org.specs2._

import sff4s.{Future, Futures, TimeoutException}

trait FutureSpec extends Specification { def is =
  "This is a specification to check a future"                                 ^
                                                                              p^
  "The sample future should"                                                  ^
    "behave like a future"                                                    ^ isFuture(future, 1)^
                                                                              endp^
  "The chained future should"                                                 ^
    "behave like a future"                                                    ^ isFuture(mapped, 2)^
                                                                              end
  
  def isFuture(v: => Future[Int], n: Int) =
    "not be defined right away"                                               ! F(v, n).e1^  
    "evaluate to %d eventually".format(n)                                     ! F(v, n).e2^
    "be defined eventually"                                                   ! F(v, n).e3^
    "throw a TimeoutException for 50 msec wait"                               ! F(v, n).e4
  
  case class F(v: Future[Int], n: Int) {
    def e1 = v.isDefined must beFalse
    def e2 = v() must be_==(n)
    def e3 = {
      val f = v
      f()
      f.isDefined must beTrue
    }
    def e4 = v(50) must throwA[TimeoutException]    
  }
  
  def factory: Futures
  def future: Future[Int] =
    factory future {
      Thread.sleep(100)
      1
    }
  def mapped =
    future map { result =>
      Thread.sleep(100)
      result + 1
    }
}
