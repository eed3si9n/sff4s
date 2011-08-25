import org.specs2._

import sff4s.Futures

class ActorsFutureSpec extends FutureSpec {
  def factory: Futures = sff4s.impl.ActorsFuture
}
