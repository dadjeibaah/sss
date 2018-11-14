package io.server.kofi

import com.twitter.conversions.storage._
import com.twitter.conversions.time._
import com.twitter.finagle.http.util.StringUtil
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.util.{Await, Future}
import io.server.kofi.servers.{ChunkedHttpServer, H2Server, IdleH2Server, LatencyServer}
import java.util.concurrent.atomic.AtomicInteger


class StreamingServer extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = {

    Future.value(Response(Status.Ok))
  }
}


object Main extends App {

  def serveH1(port: String, word: String): ListeningServer = {
    val aInt = new AtomicInteger(0);
    var toggleTimeout = false
    implicit val timer = DefaultTimer.twitter
    val service = Http.server.serve(port, Service.mk { req: Request =>
      println(req)
      for ((k, v) <- req.headerMap) {
        println(s"$k: $v")
      }
      val rsp = Response()
      rsp.contentString = ""
      rsp.statusCode = 204
      Future.value(rsp)
    })
    service
  }

  def serveFileUploader(port: String): ListeningServer = {
    implicit val timer = DefaultTimer.twitter
   Http.server.withStreaming(true).serve(s":$port", new StreamingServer)
  }

  override def main(args: Array[String]): Unit = {


    val port = s":${args(0)}"
    val word = args(1)
    val serverType = args(2)
    val h2Stream = if (args.length == 4) StringUtil.toBoolean(args(3)) else false
    val server = serverType.toLowerCase match {
      case "file"  => serveFileUploader(port)
      case "h1" => serveH1(port, word)
      case "h2" => H2Server.mk(port, h2Stream)
      case "h1-lat" => LatencyServer.mk(port)
      case "chunked" => ChunkedHttpServer.mk(port)
      case "idleh2" => IdleH2Server.mk(port)
      case _ => throw new IllegalArgumentException("Improper arguments for server startup")
    }
    Await.ready(server)
  }
}
