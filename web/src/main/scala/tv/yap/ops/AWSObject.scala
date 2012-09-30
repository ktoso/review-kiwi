package tv.yap.ops

import com.amazonaws.services.ec2.model.{Tag, Instance}
import scala.collection.JavaConversions._
import com.amazonaws.services.elasticloadbalancing.model.LoadBalancerDescription

case class AWSInstance(name:String,instanceId:String,ipAddress:String,ipPrivate:String,isFrontend:Boolean) {
  def layer = {
    if (isFrontend)
      "frontend"
    else
      ""
  }
}

object AWSInstance {

  private def value(tags:Set[Tag],name:String)  = tags.find(_.getKey == "Name").map(_.getValue)

  implicit def raw2awsinstance(raw:Instance):AWSInstance = {
    val tags = raw.getTags.toSet
    val name = value( tags , "Name").getOrElse("-not set-")
    val role = value( tags, "Roles")
    val isFrontend = role.map(_=="frontend").getOrElse(false)
    AWSInstance(name,raw.getInstanceId,raw.getPublicIpAddress,raw.getPrivateIpAddress,isFrontend)
  }
}


case class AWSBalancer(name:String,instanceIds:List[String])

object AWSBalancer {
  def raw2awsBalancer(raw:LoadBalancerDescription) = {
    AWSBalancer(raw.getDNSName,raw.getInstances.toList.map(inst=>inst.getInstanceId))
  }
}
