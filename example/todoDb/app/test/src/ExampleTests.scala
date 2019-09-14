package app
import io.undertow.Undertow

import utest._

object ExampleTests extends TestSuite{
  def withServer[T](example: cask.main.BaseMain)(f: String => T): T = {
    val server = Undertow.builder
      .addHttpListener(8080, "localhost")
      .setHandler(example.defaultHandler)
      .build
    server.start()
    val res =
      try f("http://localhost:8080")
      finally server.stop()
    res
  }

  val tests = Tests{
    test("TodoMvcDb") - withServer(TodoMvcDb){ host =>
      requests.get(s"$host/list/all").text() ==>
        """[{"id":1,"checked":true,"text":"Get started with Cask"},{"id":2,"checked":false,"text":"Profit!"}]"""
      requests.get(s"$host/list/active").text() ==>
        """[{"id":2,"checked":false,"text":"Profit!"}]"""
      requests.get(s"$host/list/completed").text() ==>
        """[{"id":1,"checked":true,"text":"Get started with Cask"}]"""

      requests.post(s"$host/toggle/2")

      requests.get(s"$host/list/all").text() ==>
        """[{"id":1,"checked":true,"text":"Get started with Cask"},{"id":2,"checked":true,"text":"Profit!"}]"""

      requests.get(s"$host/list/active").text() ==>
        """[]"""

      requests.post(s"$host/add", data = "new Task")

      requests.get(s"$host/list/active").text() ==>
        """[{"id":3,"checked":false,"text":"new Task"}]"""

      requests.post(s"$host/delete/3")

      requests.get(s"$host/list/active").text() ==>
        """[]"""
    }
  }
}
