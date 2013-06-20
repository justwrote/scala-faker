resolvers += "gseitz@github" at "http://gseitz.github.com/maven/"

addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.7")

resolvers ++= Seq(
  "lessis" at "http://repo.lessis.me",
  "coda" at "http://repo.codahale.com"
)

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.2")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.4.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.1")
