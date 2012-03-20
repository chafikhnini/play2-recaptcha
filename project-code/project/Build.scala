import sbt._
import Keys._
import PlayProject._

object ApplicationBuild extends Build {

    val appName         = "play2-recaptcha"
    val appVersion      = "1.0-SNAPSHOT"

    val appDependencies = Seq(
       "net.tanesha.recaptcha4j" % "recaptcha4j" % "0.0.7"
    )

	object Resolvers {
		// publish to my local github website clone, I will push manually
        val crionicsRepository = Resolver.file("my local repo", new java.io.File("/Users/orefalo/GitRepositories/orefalo.github.com/m2repo/releases/"))(Resolver.mavenStylePatterns) 
    }

    val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
		organization := "crionics",
		publishMavenStyle := true,
		publishTo := Some(Resolvers.crionicsRepository)
    )

}
