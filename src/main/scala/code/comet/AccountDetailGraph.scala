package code.comet

import java.util.UUID

import code.snippet.AccountDetailGraphWeeks
import net.liftweb.actor.LAFuture
import net.liftweb.common.Box.box2Option
import net.liftweb.http.js.JsCmds.SetHtml
import net.liftweb.http.CometActor
import net.liftweb.http.S
import net.liftweb.json.Serialization.read
import net.liftweb.json.Serialization
import net.liftweb.json.ShortTypeHints
import net.liftweb.util.Helpers.intToTimeSpan
import net.liftweb.util.Schedule

case class AccountDetailGraphTick()

class AccountDetailGraph extends CometActor {
  private val apiHandler = ApiHandler   
  var weekResults = scala.collection.mutable.Map[Tuple2[String, String], LAFuture[Any]]()
  var elemId: String = UUID.randomUUID.toString
  val weeksInput = S.attr("weeks").get
  
  override def localSetup {
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[AccountDetailGraphWeeks])))
    val weeks = read[AccountDetailGraphWeeks](weeksInput)
    
	weeks.weeks.map { w =>
	  weekResults(w) = apiHandler !< PropertyUpdater(w._1)
	}    
    
    println("points: " + weekResults.size)
    
    super.localSetup()
  }
  
  def render() = {
    "*" #>  <p id={elemId}>Initialized</p>
  }
  
  ping
  
  override def highPriority = { case
    AccountDetailGraphTick => {
      println("satisfaction: " + satisfaction)
      if (satisfied) {
        updateGraph
      } else {
        partialUpdate(SetHtml(elemId, <p>Updating:s {satisfaction} {UUID.randomUUID.toString}</p>))
        ping
      }
    }
  }
  
  private def updateGraph {
	weekResults.map { w =>
	  
	  println(w._2.isSatisfied)
	  
	  val future: LAFuture[Any] = w._2
	  val result = future.get(0).asA[PropertyUpdaterResult]
	  
	  partialUpdate(SetHtml(elemId, <p>gotcha</p>))
	}
  }
  
  private def satisfied:Boolean = {
    weekResults.size == satisfaction
  }
  
  private def satisfaction:Int = {
    weekResults.foldLeft(0)((a, t) => if (t._2.isSatisfied) a+1 else a)
  }
  
  private def ping {
    Schedule.schedule(this, AccountDetailGraphTick, 5000)
  }
  
}