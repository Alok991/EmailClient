name := "EmailAkka"

version := "0.1"

scalaVersion := "2.12.4"

resolvers += "lightshed-maven" at "http://dl.bintray.com/content/lightshed/maven"

libraryDependencies += "ch.lightshed" %% "courier" % "0.1.4"

// https://mvnrepository.com/artifact/com.typesafe.akka/akka-actor
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.5.7"