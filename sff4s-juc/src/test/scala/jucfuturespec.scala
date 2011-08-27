import org.specs2._

import sff4s.Futures

class JucFutureSpec extends FutureSpec { def is =
  "This is a specification to check a juc future"                             ^
                                                                              p^
  "The sample future should"                                                  ^
    "behave like a future"                                                    ^ isFuture(future, 1)^
    "behave like an async calc"                                               ^ isAsync(future)^
                                                                              endp^
  "The chained future should"                                                 ^
    "behave like a future"                                                    ^ isFuture(mapped, 2)^
    "behave like an async calc"                                               ^ isAsync(mapped)^
                                                                              end
  
  def factory: Futures = sff4s.impl.JucSingleThreadExecutorFuture
}
