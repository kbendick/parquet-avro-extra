import sbt._
import Keys._

val commonSettings = Project.defaultSettings ++ Sonatype.sonatypeSettings ++ Seq(
  organization       := "me.lyh",

  scalaVersion       := "2.11.8",
  crossScalaVersions := Seq("2.10.6", "2.11.8"),
  scalacOptions      ++= Seq("-target:jvm-1.7", "-deprecation", "-feature", "-unchecked"),
  javacOptions       ++= Seq("-source", "1.7", "-target", "1.7"),

  // Release settings
  releaseCrossBuild             := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  publishMavenStyle             := true,
  publishArtifact in Test       := false,
  sonatypeProfileName           := "me.lyh",
  pomExtra           := {
    <url>https://github.com/nevillelyh/parquet-avro-extra</url>
    <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>
    <scm>
      <connection>scm:git:git@github.com:nevillelyh/parquet-avro-extra.git</connection>
      <developerConnection>scm:git:git@github.com:nevillelyh/parquet-avro-extra.git</developerConnection>
      <url>github.com/nevillelyh/parquet-avro-extra</url>
    </scm>
    <developers>
      <developer>
        <id>sinisa_lyh</id>
        <name>Neville Li</name>
        <url>https://twitter.com/sinisa_lyh</url>
      </developer>
    </developers>
  }
)

lazy val root: Project = Project(
  "root",
  file("."),
  settings = commonSettings ++ Seq(
    run <<= run in Compile in parquetAvroExamples)
).settings(
  publish         := {},
  publishLocal    := {}
).aggregate(
  parquetAvroExtra,
  parquetAvroExamples
)

lazy val parquetAvroExtra: Project = Project(
  "parquet-avro-extra",
  file("parquet-avro-extra"),
  settings = commonSettings ++ Seq(
    libraryDependencies <+= (scalaVersion)("org.scala-lang" % "scala-reflect" % _),
    libraryDependencies ++= Seq(
      "org.apache.avro" % "avro" % "1.7.4",
      "org.apache.avro" % "avro-compiler" % "1.7.4",
      "org.apache.parquet" % "parquet-column" % "1.8.1"
    ),
    libraryDependencies := {
      CrossVersion.partialVersion(scalaVersion.value) match {
        // if Scala 2.11+ is used, quasiquotes are available in the standard distribution
        case Some((2, scalaMajor)) if scalaMajor >= 11 =>
          libraryDependencies.value
        // in Scala 2.10, quasiquotes are provided by macro paradise
        case Some((2, 10)) =>
          libraryDependencies.value ++ Seq(
            compilerPlugin("org.scalamacros" % "paradise" % "2.0.1" cross CrossVersion.full),
            "org.scalamacros" %% "quasiquotes" % "2.0.1" cross CrossVersion.binary)
      }
    }
  )
)

lazy val parquetAvroSchema: Project = Project(
  "parquet-avro-schema",
  file("parquet-avro-schema"),
  settings = commonSettings ++ sbtavro.SbtAvro.avroSettings
).settings(
  publish := {},
  publishLocal := {}
)

lazy val parquetAvroExamples: Project = Project(
  "parquet-avro-examples",
  file("parquet-avro-examples"),
  settings = commonSettings ++ Seq(
    libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.6" % "test"
  )
).settings(
  publish         := {},
  publishLocal    := {}
).dependsOn(
  parquetAvroExtra,
  parquetAvroSchema
)
