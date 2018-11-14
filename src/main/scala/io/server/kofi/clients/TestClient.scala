package io.server.kofi.clients

import com.twitter.finagle.Http

class TestClient {
  val service = Http.client.newService(":8888", "TestClient")
}
