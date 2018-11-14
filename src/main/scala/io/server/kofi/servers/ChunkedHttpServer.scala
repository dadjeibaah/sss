package io.server.kofi.servers

import com.twitter.concurrent.AsyncStream
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.{Http, ListeningServer, Service}
import com.twitter.io.{Buf, Reader, Writer}
import com.twitter.util.Future
import com.twitter.conversions.time._
import com.twitter.finagle.util.DefaultTimer

class ChunkedHttpServer extends Service[Request, Response] {
  implicit val timer = DefaultTimer

  override def apply(request: Request): Future[Response] = {
    val writable = Reader.writable()
    val Strs = AsyncStream[String]("Hello", "World", "Wassup")
    Strs.foreachF { str =>
      writable.write(Buf.Utf8(str))
    }.before(Future(writable.close()))
    Future.value(Response(request.version, Status.Ok, writable))
  }

  def writeStringChunk(str: Seq[String], writer: Writer) = {
    str.foreach { c =>
      writer.write(Buf.Utf8(c))
      Future.sleep(2.seconds)
    }
  }
}

object ChunkedHttpServer extends ServerInitializer {
  override def mk(port: String): ListeningServer =
    Http.server
      .withStreaming(enabled = true)
      .serve(port, new ChunkedHttpServer)
}
