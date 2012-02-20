package code.comet

import java.util.UUID

import code.snippet.AccountDetailGraphWeeks
import net.liftweb.actor.LAFuture
import net.liftweb.common._
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.CometActor
import net.liftweb.http.S
import net.liftweb.json.Serialization.read
import net.liftweb.json.Serialization
import net.liftweb.json.ShortTypeHints
import net.liftweb.util._

class AccountDetailGraph extends CometActor {
  private val apiHandler = ApiHandler   
  private var weekResults = Map[(String, String), Either[LAFuture[Any], PropertyUpdaterResult]]()
  private val elemId: String = Helpers.nextFuncName
  private var resultsLeft = 0

  private case class GotUpdate(week: (String, String), update: PropertyUpdaterResult)

  override def lowPriority = {
    case GotUpdate(w@(uuid, _), propertyUpdate) => {
      resultsLeft -= 1
      weekResults += w -> Right(propertyUpdate)
      if (satisfied) reRender() else partialUpdate(SetHtml(elemId, <b>found {uuid}</b>))
    }

    case AccountDetailGraphWeeks(weeks) => {
      resultsLeft = weeks.length
      
      weekResults = Map(weeks.map {
        case w@(uuid, _) => {
          val future = apiHandler !< PropertyUpdater(uuid)
          
          // what to do when the future is satisfied
          // a foreach (for comprehension without a yield)
          // will be executed on another thread when the
          // future is satisfied
          for {
            actorFutureResult <- future // get the future that is replied from the Actor !< message
            castToFuture <- Full(actorFutureResult).asA[LAFuture[PropertyUpdaterResult]] // safely cast
            propertyUpdate <- castToFuture
          } this ! GotUpdate(w, propertyUpdate)
          
          w -> Left(future)
        }
        
      } :_*)
      reRender()
    }
  }

  // get the currently satisfied results
  def allUpdates(): Map[(String, String), PropertyUpdaterResult] =
    weekResults.collect {
      case (key, Right(value)) => key -> value
    }
    
  def render() = {
    if (weekResults.isEmpty) <b>Waiting for initialization</b>
    else if (satisfied) <b>"Woo hoo... done getting data"</b> // you could do a complete render here
    else <p id={elemId}>Initialized</p>
  }
  
  private def satisfied:Boolean = resultsLeft == 0
}
