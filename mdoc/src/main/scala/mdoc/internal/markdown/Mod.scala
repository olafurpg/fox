package mdoc.internal.markdown

sealed abstract class Mod extends Product with Serializable
object Mod {
  case object Fail extends Mod
  case object Crash extends Mod
  case object Silent extends Mod
  case object Passthrough extends Mod
  case object Invisible extends Mod
  case object CompileOnly extends Mod {
    override def toString: String = "compile-only"
  }
  case object Reset extends Mod
  case object ResetClass extends Mod {
    override def toString: String = "reset-class"
  }
  case object ResetObject extends Mod {
    override def toString: String = "reset-object"
  }
  case object ToString extends Mod {
    override def toString: String = "to-string"
  }
  case object Nest extends Mod

  def all: List[Mod] = List(
    Passthrough,
    Invisible,
    CompileOnly,
    Reset,
    ResetClass,
    ResetObject,
    Fail,
    Crash,
    Silent,
    ToString,
    Nest
  )
  def unapply(string: String): Option[Mod] = {
    all.find(_.toString.equalsIgnoreCase(string))
  }
}
