package code.snippet

import net.liftweb.util.Helpers._
import net.liftweb.http.SHtml
import net.liftweb.http.S
import net.liftweb.http.DispatchSnippet
import net.liftweb.sitemap._
import net.liftweb.common.Full
import org.joda.time.Months
import net.liftweb.common.Empty
import net.liftweb.http.Templates
import org.joda.time.Weeks
import net.liftweb.json._
import net.liftweb.json.Serialization.{read, write}
import java.util.UUID

case class AccountDetailParam(accountId: String, propertyId: String)

case class AccountDetailGraphWeeks(weeks: List[Tuple2[String, String]])

class AccountDetail extends DispatchSnippet {
  def dispatch = {
    case "weeklyGraph" => weeklyGraph
    case _ => render
  }
  
  def weeklyGraph = {
    var weeksTuples:List[Tuple2[String, String]] = List()
    var i = 0
    while(i < 100) {
      weeksTuples ::= new Tuple2(UUID.randomUUID.toString, "tuple2")
      i += 1
    }
    
    implicit val formats = Serialization.formats(ShortTypeHints(List(classOf[AccountDetailGraphWeeks])))
    "*" #> <div lift={"comet?type=AccountDetailGraph&weeks=" + write(new AccountDetailGraphWeeks(weeksTuples))}></div>
  }
  def render = {
    " * "  #> "AccountDetail"
  }
}
