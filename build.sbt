import org.nlogo.build.NetLogoExtension

enablePlugins(NetLogoExtension)

version    := "1.0.0"
isSnapshot := true

scalaVersion          := "2.12.12"
Compile / scalaSource := baseDirectory.value / "src" / "main"
Test / scalaSource    := baseDirectory.value / "src" / "test"
scalacOptions        ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-encoding", "UTF-8", "-release", "11")

// Add custom resolvers for dependencies
resolvers ++= Seq(
  "Central" at "https://repo1.maven.org/maven2/",
  "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases"
)

// Add dependencies
libraryDependencies ++= Seq(
  "org.scalaj" %% "scalaj-http" % "2.4.2",     // HTTP client
  "org.json4s" %% "json4s-native" % "3.6.12",  // JSON processing
  "org.json4s" %% "json4s-jackson" % "3.6.12",  // JSON processing Jackson backend
  "org.json4s" %% "json4s-core" % "3.6.12"    // JSON processing core
)

netLogoExtName      := "openai"
netLogoClassManager := "org.nlogo.extensions.openai.OpenAIExtension"
netLogoVersion      := "6.3.0" 