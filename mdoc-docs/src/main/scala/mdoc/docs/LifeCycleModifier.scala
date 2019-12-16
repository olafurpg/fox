package mdoc.docs

import mdoc._

/**
  * Global counter used to test the [[mdoc.Main]] process counting.
  */
object LifeCycleCounter {
  val numberOfStarts: ThreadLocal[Integer] = ThreadLocal.withInitial(() => 0)
  val numberOfExists: ThreadLocal[Integer] = ThreadLocal.withInitial(() => 0)
}

class LifeCycleModifier extends PostModifier {
  val name = "lifecycle"

  // Starts and stops per instance
  var numberOfStarts = 0
  var numberOfExists = 0
  // Pre and post processing per instance
  var numberOfPreProcess = 0
  var numberOfPostProcess = 0

  def process(ctx: PostModifierContext): String = {
    // Used for checking the counting
    s"numberOfStarts = $numberOfStarts ; numberOfExists = $numberOfExists ; numberOfPreProcess = $numberOfPreProcess ; numberOfPostProcess = $numberOfPostProcess"
  }

  /**
    * This is called once when the [[mdoc.Main]] process starts
    * @param settings CLI or API settings used by mdoc
    */
  override def onStart(settings: MainSettings): Unit = {
    numberOfStarts += 1
    LifeCycleCounter.numberOfStarts.set(LifeCycleCounter.numberOfStarts.get() + 1)
  }

  override def preProcess(ctx: PostModifierContext): Unit = { numberOfPreProcess += 1 }

  override def postProcess(ctx: PostModifierContext): Unit = { numberOfPostProcess += 1 }

  /**
    * This is called once when the [[mdoc.Main]] process finsihes
    * @param exit is the exit code returned by mdoc's processing
    */
  override def onExit(exit: Int): Unit = {
    numberOfExists += 1
    LifeCycleCounter.numberOfExists.set(LifeCycleCounter.numberOfExists.get() + 1)
  }
}
