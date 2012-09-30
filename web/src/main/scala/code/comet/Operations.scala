package code.comet

import net.liftweb.http.CometActor
import net.liftweb.http.js.JsCmds.SetHtml
import scala.xml.{NodeSeq, Text}
import net.liftweb.util._
import Helpers._
import tv.yap.ops.{AWSBalancer, AWSInstance, AWSManager}
import akka.dispatch.Future

sealed trait ClusterMessage

case class UpdateInstances(instances:Seq[AWSInstance])
case class UpdateBalancers(balancers:Seq[AWSBalancer])

class Operations extends CometActor {

  val awsManager = AWSManager.instance

  Future {
    awsManager.fetchInstances
  } foreach(this ! UpdateInstances(_))

  Future {
    awsManager.fetchLoadBalancers
  } foreach( this ! UpdateBalancers(_))

  def render = {
    "#instances *" #> renderInstances(awsManager.instances.get()) &
      "#elbs *" #> renderBalancers(awsManager.balancers.get())
  }

  private def renderInstances(instances:Seq[AWSInstance]) = instances.map{ instance=>
    <tr><td>{instance.name}</td>
      <td>{instance.instanceId}</td>
      <td>{instance.name}</td>
      <td>{instance.layer}</td>
    </tr>
    }.toSeq

  private def renderBalancers(balancers:Seq[AWSBalancer]) = balancers.map{balancer=>
    <tr>
      <td>{balancer.name}</td>
      <td>{balancer.instanceIds.mkString(" ")}</td>
    </tr>
    }.toSeq

  override def lowPriority = {

    case UpdateInstances(instances) =>
      partialUpdate(SetHtml("instances",renderInstances(instances)))

    case UpdateBalancers(balancers) =>
      partialUpdate(SetHtml("elbs",renderBalancers(balancers)))
  }

}

