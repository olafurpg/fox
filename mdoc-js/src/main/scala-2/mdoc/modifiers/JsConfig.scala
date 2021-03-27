package mdoc.modifiers

import mdoc.OnLoadContext
import mdoc.PostProcessContext
import mdoc.internal.pos.PositionSyntax._
import org.scalajs.linker.interface.ModuleKind
import org.scalajs.logging.Level
import scala.meta.internal.io.PathIO
import scala.meta.io.AbsolutePath
import scala.meta.io.Classpath

case class JsConfig(
    moduleKind: ModuleKind = ModuleKind.NoModule,
    htmlHeader: String = "",
    libraries: List[AbsolutePath] = Nil,
    mountNode: String = "node",
    minLevel: Level = Level.Info,
    outDirectories: List[AbsolutePath] = List(PathIO.workingDirectory),
    outPrefix: Option[String] = None,
    fullOpt: Boolean = true,
    htmlPrefix: String = "",
    relativeLinkPrefix: String = "",
    batchMode: Boolean = false
) {
  def isCommonJS: Boolean = moduleKind == ModuleKind.CommonJSModule
  def libraryScripts(
      outjsfile: AbsolutePath,
      ctx: PostProcessContext
  ): Iterable[String] = {
    val library = libraries.find(_.filename.endsWith("-library.js"))
    val loader = libraries.find(_.filename.endsWith("-loader.js"))
    val sourcemaps = libraries.filter(_.filename.endsWith(".js.map"))
    val all = List(library.toList, loader.toList, sourcemaps).flatten
    all.flatMap { lib =>
      val filename = lib.filename
      val out = outjsfile.resolveSibling(_ => filename)
      if (filename.endsWith(".js")) {
        lib.copyTo(out)
        val src = out.toRelativeLinkFrom(ctx.outputFile, relativeLinkPrefix)
        List(s"""<script type="text/javascript" src="$src" defer></script>""")
      } else if (filename.endsWith(".js.map")) {
        lib.copyTo(out)
        Nil
      } else {
        Nil
      }
    }
  }
}

object JsConfig {
  def fromVariables(ctx: OnLoadContext): JsConfig = {
    val base = JsConfig()
    JsConfig(
      ctx.site.get("js-module-kind") match {
        case None => base.moduleKind
        case Some(value) =>
          value match {
            case "NoModule" => ModuleKind.NoModule
            case "CommonJSModule" => ModuleKind.CommonJSModule
            case "ESModule" => ModuleKind.ESModule
            case unknown =>
              ctx.reporter.error(s"unknown 'js-module-kind': $unknown")
              base.moduleKind
          }
      },
      ctx.site.getOrElse("js-html-header", ""),
      Classpath(ctx.site.getOrElse("js-libraries", "")).entries,
      mountNode = ctx.site.getOrElse("js-mount-node", base.mountNode),
      outDirectories = ctx.settings.out,
      outPrefix = ctx.site.get("js-out-prefix"),
      minLevel = ctx.site.get("js-level") match {
        case None => Level.Info
        case Some("info") => Level.Info
        case Some("warn") => Level.Warn
        case Some("error") => Level.Error
        case Some("debug") => Level.Debug
        case Some(unknown) =>
          ctx.reporter.warning(s"unknown 'js-level': $unknown")
          Level.Info
      },
      fullOpt = ctx.site.get("js-opt") match {
        case None =>
          !ctx.settings.watch
        case Some(value) =>
          value match {
            case "fast" => false
            case "full" => true
            case unknown =>
              ctx.reporter.error(s"unknown 'js-opt': $unknown")
              !ctx.settings.watch
          }
      },
      relativeLinkPrefix = ctx.site.getOrElse("js-relative-link-prefix", base.relativeLinkPrefix),
      batchMode = ctx.site.getOrElse("js-batch-mode", "false").toBoolean
    )
  }
}
