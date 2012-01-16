import org.specs2._

import sff4s.{Future, Futures}

class TwitterUtilFutureSpec extends FutureSpec { def is =
  "This is a specification to check a twitter-util future"                    ^
                                                                              p^
  "The sample future should"                                                  ^
    "behave like a future"                                                    ^ isFuture(future, 1)^
                                                                              endp^
  "The bad future should"                                                     ^
    "behave like a bad future"                                                ^ isBadFuture(bad, future)^
                                                                              endp^
  "The chained future should"                                                 ^
    "behave like a future"                                                    ^ isFuture(mapped, 2)^
                                                                              endp^
  "The converted future should"                                               ^
    "behave like a future"                                                    ^ isFuture(converted, 3)^
                                                                              end
  
  def factory: Futures = sff4s.impl.TwitterUtilFuture
  def converted: Future[Int] = {
    sff4s.impl.TwitterUtilFuture toFuture(com.twitter.util.Future.value({
      Thread.sleep(100)
      3
    }))
  }
}
