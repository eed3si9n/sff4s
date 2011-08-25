import org.specs2._

import sff4s.{Future, Futures, TimeoutException}

trait FutureSpec extends Specification { def is =
  "This is a specification to check a future"                                 ^
                                                                              p^
  "The sample future should"                                                  ^
    "be not be defined right away"                                            ! e1^
    "evaluate to 1 eventually "                                               ! e2^
    "be defined eventually"                                                   ! e3^
    "throw a TimeoutException for 50 msec wait"                               ! e4^
                                                                              end
  
  def e1 = future.isDefined must beFalse
  def e2 = future() must be_==(1)
  def e3 = {
    val f = future
    f()
    f.isDefined must beTrue
  }
  def e4 = future(50) must throwA[TimeoutException]
  
  def factory: Futures
  def future: Future[Int] = factory future {
    Thread.sleep(100)
    1
  }
}
