package bootstrap.liftweb

import akka.config.Configuration
import tv.yap.common.s3.S3Config

trait AssetsConfig {
  def roviAssetsAwsBucket: String
  def roviAssetURLPrefix: String
  def roviAssetFolder: String
}

trait LiftedConfig extends S3Config
  with AssetsConfig {

  def apiEndpoint: String
}

object LiftedConfig {
  private var theConfig: LiftedConfig = _

  def initFromAkkaConfig(rootConfig: Configuration) {
    theConfig = new LiftedConfig {
      val liftedConfig = rootConfig.getSection("lifted").getOrElse(Configuration.fromMap(Map()))
      val apiEndpoint = liftedConfig.getString("api-endpoint").getOrElse("https://api.yap.tv/t")

      val s3Config = S3Config.section(rootConfig)
      val awsAccessKeyId = S3Config.awsAccessKeyId(s3Config)
      val awsSecretKey = S3Config.awsSecretKey(s3Config)
      val awsPoolSize = S3Config.awsPoolSize(s3Config)
      val awsBucketName = S3Config.awsBucketName(s3Config)
      val awsBucketPrefix = S3Config.awsBucketPrefix(s3Config)

      val roviConfig = rootConfig.getSection("rovi").get

      val roviAssetsConfig = roviConfig.getSection("assets").get
      val roviAssetsAwsBucket = roviAssetsConfig.getString("target-bucket").get
      val roviAssetURLPrefix = roviAssetsConfig.getString("url-prefix").get
      val roviAssetFolder = roviAssetsConfig.getString("folder").getOrElse("assets")
    }
  }

  def is = theConfig
}
