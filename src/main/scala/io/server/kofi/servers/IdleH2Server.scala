package io.server.kofi.servers

import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.H2
import com.twitter.finagle.buoyant.h2.{Frame, Headers, Request, Response, Status, Stream}
import com.twitter.util.Future

class IdleH2Server extends Service[Request, Response] {
  override def apply(request: Request): Future[Response] = {
      Future.value(Response(Status.Ok, Stream()))
  }
}
object IdleH2Server {
  def mk(addr: String) = H2.serve(addr, new IdleH2Server)
}
