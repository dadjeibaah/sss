import sbtassembly.MergeStrategy

val defaultMergeStrategy: String => MergeStrategy = {
  case x if Assembly.isConfigFile(x) =>
    MergeStrategy.concat
  case PathList(ps @ _*) if Assembly.isReadme(ps.last) || Assembly.isLicenseFile(ps.last) =>
    MergeStrategy.rename
  case PathList("META-INF", xs @ _*) =>
    (xs map {_.toLowerCase}) match {
      case ("manifest.mf" :: Nil) | ("index.list" :: Nil) | ("dependencies" :: Nil) =>
        MergeStrategy.discard
      case ps @ (x :: xs) if ps.last.endsWith(".sf") || ps.last.endsWith(".dsa") =>
        MergeStrategy.discard
      case "plexus" :: xs =>
        MergeStrategy.discard
      case "services" :: xs =>
        MergeStrategy.filterDistinctLines
      case ("spring.schemas" :: Nil) | ("spring.handlers" :: Nil) =>
        MergeStrategy.filterDistinctLines
      case _ => MergeStrategy.deduplicate
    }
  case x if x.endsWith("io.netty.versions.properties") => MergeStrategy.first
  case _ => MergeStrategy.deduplicate
}

enablePlugins(DockerPlugin)

dockerfile in docker := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}
lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.5"
)

lazy val server = (project in file(".")).settings(
  name := "sss-server",
  commonSettings,
  libraryDependencies ++= Seq(
    "com.twitter" %% "finagle-http" % "7.1.0",
    "io.buoyant" %% "finagle-h2" % "1.3.6"
  )
).settings(
  assemblyMergeStrategy in assembly := defaultMergeStrategy
)

lazy val traceApp = (project in file("./tracer")).settings(
  name := "traceApp",
  commonSettings,
  mainClass in (Compile,run) := Some("io.tracer.Main"),
  libraryDependencies ++= Seq(
    ("com.twitter" %% "finagle-http" % "7.1.0")
      .exclude("io.buoyant", "finagle-h2")
  ),
  assemblyMergeStrategy in assembly := defaultMergeStrategy
)


