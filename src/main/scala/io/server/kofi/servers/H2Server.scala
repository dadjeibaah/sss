package io.server.kofi.servers

import com.twitter.conversions.time._
import com.twitter.finagle.Service
import com.twitter.finagle.buoyant.h2.{Frame, Request, Response, Status, Stream}
import com.twitter.util.Future
import com.twitter.finagle.buoyant.h2._
import com.twitter.finagle.buoyant.H2
import com.twitter.finagle.util.DefaultTimer

class H2Server(isStream: Boolean) extends Service[Request, Response]() {
  implicit val timer = DefaultTimer
  override def apply(request: Request): Future[Response] = {

    if (isStream){
      println(toString(request))
      for ((k, v) <- request.headers.toSeq) {
        println(s"$k: $v")
      }
      println()


      val status = Status.Accepted

      val q = Stream()

      def loop(current: Int, end: Int): Future[Unit] = {
        if (current > end) {
          q.write(Frame.Data(s"$current", eos = true))
          return Future.Unit
        }
        q.write(Frame.Data(s"$current", eos = false))
        Future.sleep(2.second).before(loop(current + 1, end))
        Future.Unit
      }

      loop(1, 10)
      val rsp = Response(status, q)
      Future.value(rsp)
    } else {
      val resp = Response(Headers(Seq((":status", Status.Accepted.toString), ("l5d-random-context","This should not appear"))), Stream.const(s"App received diagnostic TRACE\n"))
      Future.value(resp)
    }


  }

  def toString(req: Request): String = {
    val method = req.headers.get(Headers.Method).getOrElse("???")
    val authority = req.headers.get(Headers.Authority).getOrElse("???")
    val path = req.headers.get(Headers.Path).getOrElse("???")
    s"$method $authority$path"
  }

}

object H2Server{
  def mk(addr: String, stream: Boolean = false) = H2.serve(addr, new H2Server(stream))
}
