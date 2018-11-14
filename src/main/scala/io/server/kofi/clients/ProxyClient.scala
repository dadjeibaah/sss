package io.server.kofi.clients

import com.twitter.finagle.client.Transporter
import com.twitter.finagle.{Http, Service}
import com.twitter.finagle.http.{Request, Response}

class ProxyClient {
  val twitter: Service[Request, Response] = Http.client
    .withTransport.httpProxyTo(
    host = "twitter.com:443",
    credentials = Transporter.Credentials("user", "password")
  )
    .newService("inet!my-proxy-server.com:3128") // using local DNS to resolve proxy
}
