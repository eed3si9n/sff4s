import org.specs2.mutable._

class ToFutureBugSpec extends Specification {

  "#toFuture" should {
    "accept java.util.concurrent.Future" in {
      import java.util.concurrent.{FutureTask, Callable}
      val jucFuture = new FutureTask(new Callable[String]() { def call(): String = "foo" })
      jucFuture.run()
      val factory = sff4s.impl.JucSingleThreadExecutorFuture
      val future = factory.toFuture(jucFuture)
      future() mustEqual "foo"
    }
  }

}
