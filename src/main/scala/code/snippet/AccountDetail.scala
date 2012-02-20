package code.snippet

import net.liftweb.util._
import Helpers._
import net.liftweb.http._
import net.liftweb.sitemap._
import net.liftweb.common._
import java.util.UUID
import xml._

case class AccountDetailParam(accountId: String, propertyId: String)

case class AccountDetailGraphWeeks(weeks: List[(String, String)])

class AccountDetail extends DispatchSnippet {
  def dispatch = {
    case "weeklyGraph" => weeklyGraph
    case _ => render
  }
  
  def weeklyGraph(in: NodeSeq): NodeSeq = {
    // initialize the CometActor
    for {
      sess <- S.session
    } sess.sendCometActorMessage("AccountDetailGraph", Empty,
                                 AccountDetailGraphWeeks( (1 to 100).toList.map(_ => (UUID.randomUUID.toString, "tuple2"))))

    <lift:comet type="AccountDetailGraph">{in}</lift:comet>
  }
  def render = {
    " * "  #> "AccountDetail"
  }
}
