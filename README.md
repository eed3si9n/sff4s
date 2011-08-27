sff4s
=====

sff4s (simple future facade for Scala) is a Scala wrapper around several future implementations.

The API mostly mimics that of twitter util's [`Future`][1]:

```scala
val factory = sff4s.impl.ActorsFuture
val f = factory future {
  Thread.sleep(1000)
  1
}
val g = f map { _ + 1 }
g(2000) // => This blocks for the futures result (and eventually returns 2)

// Another option:
g onSuccess { value =>
  println(value) // => prints "2"
}

// Using for expressions:
val xFuture = factory future {1}
val yFuture = factory future {2}

for {
  x <- xFuture
  y <- yFuture
} {
  println(x + y) // => prints "3"
}

// Using implicit conversion
import factory._
val native = scala.actors.Futures future {5}
val w: sff4s.Future[Int] = native
w() // => This blocks for the futures result (and eventually returns 5)
```

sff4s-api
---------
Platform independent API consisting of `abstract class Future[+A]`, which represents a future value; and `trait Futures`, which represents the dispatcher to create the future values.

sff4s-actors
------------
Wrapper around [`scala.actors.Future`][2].

sff4s-juc
---------
Wrapper around [`java.util.concurrent.Future`][3].

sff4s-akka
----------
Wrapper around [`akka.dispatch.Future`][4].

sff4s-twitter-util
------------------
Wrapper around [`com.twitter.util.Future`][5]. Note unlike other implementation, `TwitterUtilFuture.future` does not process the calculation in the background. Instead, you're supposed to create a `Promise` object and set the value using your own concurrency mechanism.

  [1]: https://github.com/twitter/util/blob/master/util-core/src/main/scala/com/twitter/util/Future.scala
  [2]: http://www.scala-lang.org/api/current/scala/actors/Future.html
  [3]: http://download.oracle.com/javase/6/docs/api/java/util/concurrent/Future.html
  [4]: http://akka.io/api/akka/1.1/akka/dispatch/Future.html
  [5]: http://twitter.github.com/util/util-core/target/site/doc/main/api/com/twitter/util/Future.html
  