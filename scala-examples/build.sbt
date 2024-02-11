//Scala 3 project to exemplify the use of lang4j with Scala
name := "scala-examples"

val langchain4jVersion = "0.27.1"
val tinylogVersion     = "2.6.2"

version := langchain4jVersion

scalaVersion := "3.3.1"

libraryDependencies ++= Seq(
  "org.scala-lang" %% "toolkit"                                   % "0.2.1",
  "dev.langchain4j" % "langchain4j"                               % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-open-ai"                       % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-hugging-face"                  % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-vertex-ai"                     % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-embeddings"                    % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-embeddings-all-minilm-l6-v2"   % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-document-parser-apache-pdfbox" % langchain4jVersion,
  "dev.langchain4j" % "langchain4j-document-parser-apache-poi"    % langchain4jVersion,
  "org.mapdb"       % "mapdb"                                     % "3.0.9" exclude ("org.jetbrains.kotlin", "kotlin-stdlib"),
  "org.tinylog"     % "tinylog-impl"                              % tinylogVersion,
  "org.tinylog"     % "slf4j-tinylog"                             % tinylogVersion,
)
