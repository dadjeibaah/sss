package io.server.kofi.clients

import com.twitter.finagle.Http
import com.twitter.finagle.http.Request
import com.twitter.util.Await

object ClientMain extends App {
  override def main(args: Array[String]): Unit = {
    val service = Http.client.newService(":8888", "TestClient")

    val req = Request()
    val rsp = Await.result(service(req))
    println("Response Headers Received")
    println(rsp.headerMap)
  }
}
