package code.comet


import net.liftweb.actor.LiftActor
import net.liftweb.common.Box
import net.liftweb.http.CometActor
import net.liftweb.util.Helpers.intToTimeSpanBuilder
import net.liftweb.util.Schedule
import net.liftweb.common.Full
import collection.JavaConversions._
import scala.collection.immutable.Queue
import net.liftweb.actor.LAFuture
import java.util.UUID

case class Tick()
case class PropertyUpdater(foo: String)
case class PropertyUpdaterResult(response: Map[String, String])
case class PropertyUpdaterIntermediate(reply: LAFuture[PropertyUpdaterResult], prop: PropertyUpdater)

object ApiHandler extends LiftActor {
  
  private var updaters = Queue.empty[PropertyUpdaterIntermediate]
  
  private def tickHandler {
    if (updaters.isEmpty) {
      Schedule.schedule(this, Tick, 5 seconds)
      return
    }
    
    val (intermediate, updatersTemp) = updaters.dequeue
    updaters = updatersTemp
    
    val property = intermediate.prop
    val repl = intermediate.reply
    
    // faking an api call
    Thread.sleep(10000)
    
    val data = Map[String, String](UUID.randomUUID.toString -> "123", UUID.randomUUID.toString -> "456")
    repl.satisfy(new PropertyUpdaterResult(data))
    
    Schedule.schedule(this, Tick, 10 seconds)
  }
  
  Schedule.schedule(this, Tick, 10 seconds)
  
  override def messageHandler = {
    case p@PropertyUpdater(_) =>
      val rep: LAFuture[PropertyUpdaterResult] = new LAFuture[PropertyUpdaterResult]
      val int = PropertyUpdaterIntermediate(rep, p)
      updaters = updaters.enqueue(int)
      reply(rep)
    case Tick => tickHandler
  }
}