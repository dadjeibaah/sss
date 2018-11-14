# Simple Scala Server

A toy server application built with Finagle that can be used for testing. Originally created by [adleong](https://github.com/adleong)

### Running it locally
To run it locally ensure you have sbt installed on your machine then, in a terminal, run:
```bash
sbt 'server/run 8888 word h1'
```

This runs an HTTP server on port 8888 and responds with the word, "word"
