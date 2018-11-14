package io.server.kofi.servers

import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response, Status}
import com.twitter.finagle.util.DefaultTimer
import com.twitter.io.Buf
import com.twitter.util._

class LatencyServer extends Service[Request, Response] {
  implicit val timer = DefaultTimer

  override def apply(request: Request): Future[Response] = {
    Try(Duration.parse(request.getParam("latency"))) match {
      case Throw(e) => Future.value(LatencyServer.BadResponse())
      case Return(r) =>
        Future
          .sleep(r)
          .before(
            Future.
              value(LatencyServer.GoodResponse(s"waited $r to respond"))
          )
    }
  }
}

object LatencyServer {
  def BadResponse(): Response = {
    val resp = Response(Status.BadRequest)
    resp.content(Buf.Utf8("Unparsable duration"))
    resp
  }

  def GoodResponse(msg: String) = {
    val resp = Response(Status.Ok)
    resp.content(Buf.Utf8(msg))
    resp
  }

  def mk(port: String) = {
    Http.serve(port, new LatencyServer)
  }
}
