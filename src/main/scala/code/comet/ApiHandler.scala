package code.comet


import net.liftweb.actor._
import net.liftweb.common._
import net.liftweb.util._
import Helpers._
import java.util.UUID

case class PropertyUpdater(foo: String)
case class PropertyUpdaterResult(response: Map[String, String])
case class PropertyUpdaterIntermediate(reply: LAFuture[PropertyUpdaterResult], prop: PropertyUpdater)

object ApiHandler extends LiftActor {
  override def messageHandler = {
    case p@PropertyUpdater(_) => {
      // create the future
      val rep = new LAFuture[PropertyUpdaterResult]

      // schedule the satisfaction of the future some time between 1 and 6 seconds from now
      Schedule(() => {
        val data = Map(UUID.randomUUID.toString -> "123", UUID.randomUUID.toString -> "456")
        rep.satisfy(PropertyUpdaterResult(data))
      }, Helpers.randomLong(5000) + 1000L)

      // reply with the future
      reply(rep)
    }

  }
}
