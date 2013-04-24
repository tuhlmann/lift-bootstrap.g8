name := "$name;format="norm"$"

scalaVersion := "2.10.1"

resolvers ++= Seq(
  "Liftmodules repo"               at "https://repository-liftmodules.forge.cloudbees.com/release",
  "Java.net Maven2 Repository"     at "http://download.java.net/maven/2/",
  "Sonatype scala-tools repo"      at "https://oss.sonatype.org/content/groups/scala-tools/",
  "Sonatype scala-tools releases"  at "https://oss.sonatype.org/content/repositories/releases",
  "Sonatype scala-tools snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

{
  val liftVersion = "2.5-RC5"
  libraryDependencies ++= Seq(
    "net.liftweb"             %% "lift-webkit"       % liftVersion             % "compile",
    "net.liftweb"             %% "lift-mapper"       % liftVersion             % "compile",
    "ch.qos.logback"          %  "logback-classic"   % "1.0.6",
    "junit"                   %  "junit"             % "4.7"                   % "test",
    "org.specs2"              %% "specs2"            % "1.14"                  % "test",
    "org.pegdown"             %  "pegdown"           % "1.1.0"                 % "test",
    "org.scalatest"           %% "scalatest"         % "2.0.M5b"               % "test",
    "cc.co.scala-reactive"    %% "reactive-core"     % "0.3.0" intransitive(),
    "cc.co.scala-reactive"    %% "reactive-web"      % "0.3.0" intransitive(),
    "org.eclipse.jetty"       %  "jetty-webapp"      % "8.1.7.v20120910"       %  "container",
    "net.liftmodules"         %% "mapperauth_2.5"    % "0.1-SNAPSHOT"          %  "compile",
    "net.liftmodules"         %% "widgets_2.5"       % "1.3-SNAPSHOT"          %  "compile",
    "net.liftmodules"         %% "extras_2.5"        % "0.1"                   %  "compile",
    "com.h2database"          %  "h2"                % "1.2.138"
  )
}

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "$package$"

seq(lessSettings:_*)

(LessKeys.filter in (Compile, LessKeys.less)) := "styles.less"

(LessKeys.mini in (Compile, LessKeys.less)) := true

seq(closureSettings:_*)

(ClosureKeys.prettyPrint in (Compile, ClosureKeys.closure)) := false

seq(webSettings :_*)

// add managed resources, where less and closure publish to, to the webapp
(webappResources in Compile) <+= (resourceManaged in Compile)

// Remove Java directories, otherwise sbteclipse generates them
unmanagedSourceDirectories in Compile <<= (scalaSource in Compile)(Seq(_))

unmanagedSourceDirectories in Test <<= (scalaSource in Test)(Seq(_))

EclipseKeys.withSource := true

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource
