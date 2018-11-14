package io.server.kofi.servers

import com.twitter.finagle.ListeningServer

trait ServerInitializer {
  def mk(port: String): ListeningServer
}
