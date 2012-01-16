import org.specs2._

import sff4s.{Future, Futures}

class ActorsFutureSpec extends FutureSpec { def is =
  "This is a specification to check an actors future"                         ^
                                                                              p^
  "The sample future should"                                                  ^
    "behave like a future"                                                    ^ isFuture(future, 1)^
    "behave like an async calc"                                               ^ isAsync(future)^
                                                                              endp^
  "The bad future should"                                                     ^
    "behave like a bad future"                                                ^ isBadFuture(bad, future)^
                                                                              endp^
  "The chained future should"                                                 ^
    "behave like a future"                                                    ^ isFuture(mapped, 2)^
    "behave like an async calc"                                               ^ isAsync(mapped)^
                                                                              endp^
  "The converted future should"                                               ^
    "behave like a future"                                                    ^ isFuture(converted, 3)^
    "behave like an async calc"                                               ^ isAsync(converted)^
                                                                              end
  
  def factory: Futures = sff4s.impl.ActorsFuture
  def converted: Future[Int] = {
    sff4s.impl.ActorsFuture toFuture(scala.actors.Futures.future { 
      Thread.sleep(100)
      3
    })
  }
}
