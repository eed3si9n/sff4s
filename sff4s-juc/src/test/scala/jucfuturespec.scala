import org.specs2._

import sff4s.{Future, Futures}

class JucFutureSpec extends FutureSpec { def is =
  "This is a specification to check a juc future"                             ^
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
  
  def factory: Futures = sff4s.impl.JucSingleThreadExecutorFuture
  def converted: Future[Int] = {
    import java.util.concurrent.{FutureTask, Callable}
    val jucFuture = new FutureTask(new Callable[Int]() { 
      def call(): Int = {
        Thread.sleep(100)
        3 
      }
    })
    jucFuture.run()
    sff4s.impl.JucSingleThreadExecutorFuture toFuture(jucFuture)
  }
}
