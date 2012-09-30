package tv.yap.ops

import com.amazonaws.services.ec2.AmazonEC2AsyncClient
import com.amazonaws.auth.{BasicAWSCredentials}
import com.amazonaws.services.ec2.model.{DescribeInstancesResult, DescribeInstancesRequest, DescribeAvailabilityZonesRequest}
import scala.collection.JavaConversions._
import java.util.concurrent.{Future => JavaFuture, TimeoutException, CancellationException, TimeUnit}
import com.amazonaws.services.ec2.model.{Reservation, DescribeInstancesResult, DescribeAvailabilityZonesResult}
import java.util.concurrent.atomic.AtomicReference
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingAsyncClient
import com.amazonaws.services.elasticloadbalancing.model.DescribeLoadBalancersRequest


class  AWSManager private() {
  val AWS_KEY="AKIAJ7GAOQO4C7NSJAOA"
  val AWS_SECRET="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"
  val availabilityZones = List("us-east1a","us-east1b","us-east1c","us-east1d","us-east1a")

  val instances = new AtomicReference[List[AWSInstance]](List())
  val balancers = new AtomicReference[List[AWSBalancer]](List())

  val credentials = new BasicAWSCredentials(AWS_KEY,AWS_SECRET)

  lazy val ec2Client = {
    new AmazonEC2AsyncClient(credentials)
  }

  lazy val elbClient = new AmazonElasticLoadBalancingAsyncClient(credentials)

  /*
   * Exploratory code to integrate Java futures.
   * Will probably end up going with JClouds.
   *
  var listInstancesFuture:Option[JavaFuture[DescribeInstancesResult]] = None

  private def peek[T](maybe:Option[JavaFuture[T]]):(Option[JavaFuture[T]],Option[T]) = {
    try {
      val result = maybe.map(_.get(10,TimeUnit.MILLISECONDS))
      (None,result)
    } catch {
      case e:TimeoutException =>
        (maybe,None)
      case e:InterruptedException =>
        (maybe,None)
      case _ =>
        (None,None)
    }
  }
  */

  def fetchInstances:Seq[AWSInstance] = {
    val fetch = ec2Client.describeInstancesAsync(new DescribeInstancesRequest())
    val fetched = fetch.get().getReservations.flatMap(_.getInstances).map(AWSInstance.raw2awsinstance(_)).toList
    instances.set( fetched )
    fetched
  }

  def describeAvailabilityZones = {
    ec2Client.describeAvailabilityZonesAsync(new DescribeAvailabilityZonesRequest().withZoneNames(availabilityZones))
  }

  def fetchLoadBalancers:Seq[AWSBalancer] = {
    val fetch = elbClient.describeLoadBalancersAsync(new DescribeLoadBalancersRequest())
    val fetched = fetch.get().getLoadBalancerDescriptions.map(AWSBalancer.raw2awsBalancer(_)).toList
    balancers.set(fetched)
    fetched
  }


  val hostsFooter = """
    |192.168.244.1        us-west1
    |
    |# The following lines are desirable for IPv6 capable hosts
    |::1       ip6-localhost ip6-loopback
    |fe00::0   ip6-localnet
    |ff00::0   ip6-mcastprefix
    |ff02::1   ip6-allnodes
    |ff02::2   ip6-allrouters
    |ff02::3   ip6-allhosts
    |
  """.stripMargin

  def generateHosts(current:String) = {
    "%-20s localhost %s\n".format("127.0.0.1",current) +
    instances.get().filter(_.name != current).map(a=>"%-20s %s".format(a.ipPrivate,a.name)).mkString("\n") +
    hostsFooter
  }

}

object AWSManager  {
  lazy val instance = new AWSManager;
}
