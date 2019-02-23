name := """rate-conversion-api"""

scalaVersion := "2.12.8"

resolvers ++= Seq(
  Resolver.jcenterRepo
)

libraryDependencies ++= Seq(
  ws,
  guice,
  "net.codingwell" %% "scala-guice" % "4.2.1",
  "org.webjars" % "swagger-ui" % "2.2.0",
  "org.scalaz" %% "scalaz-core" % "7.2.27",
)

lazy val root = (project in file(".")).enablePlugins(PlayScala, SwaggerPlugin)

swaggerDomainNameSpaces := Seq("models")
