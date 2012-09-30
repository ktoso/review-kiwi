package code.snippet

import net.liftweb.http.rest.RestHelper
import net.liftweb.http._
import net.liftweb.util.StringHelpers
import net.liftweb.common.{Logger, Full, Box}
import net.liftweb.json.JsonAST._
import net.liftweb.json.JsonDSL._
import net.liftweb.http.InMemoryResponse
import tv.yap.services.show.ShowcardProcessor
import bootstrap.liftweb.LiftedConfig
import tv.yap.rovi.shows.RoviImageTagShowUpdater
import tv.yap.rovi.YapProgramMasterDetector
import upload.AjaxImageUpload
import com.google.common.io.{Files, ByteStreams}
import tv.yap.common.s3.S3Uploader
import java.io.File
import tv.yap.model.messaging.{AssetProvider, YapShow}
import tv.yap.ModelServices

class ShowcardImageUpload(showResourceId: Int) extends AjaxImageUpload {

  import ShowcardImageUploadHandler._

  def targetUrl = "/" + uploadUrl

  val params = Map("showId" -> Full(showResourceId))
  override lazy val paramsString = targetUrl + "/showId/%d".format(params("showId").get)
}

object ShowcardImageUploadHandler extends RestHelper with ModelServices with Logger {

  val ShowcardPrefix = "l"

  val uploadUrl = "upload-showcard"

  lazy val showcardProcessor = new ShowcardProcessor(
    LiftedConfig.is,
    LiftedConfig.is.roviAssetsAwsBucket,
    roviImageTagShowUpdater,
    overrideTagPrefix = Some(ShowcardPrefix)
  )

  lazy val ImageHandlers = {
    import showcardProcessor._
    LandscapeImageHandler :: Nil
  }

  val ErrorResponse = ("name" -> "n/a") ~ ("type" -> "n/a") ~ ("size" -> 0L)

  serve {
    case ShowcardImageUploadHandler.uploadUrl :: "showId" :: showId :: Nil Post req => {
      val originalFilename = req.param("qqfile")

      val ojv: Box[JValue] = req.body.flatMap(bytes => processImage(showId.toInt, bytes, originalFilename)).headOption

      val ret = ojv openOr ErrorResponse

      val json = JsonResponse(ret).toResponse.asInstanceOf[InMemoryResponse]
      InMemoryResponse(json.data, ("Content-Length", json.data.length.toString) ::("Content-Type", "application/json") :: Nil, Nil, 200)
    }
  }

  // todo, fire this of to an actor?
  def processImage(showId: Int, bytes: Array[Byte], originalFilename: Option[String]): Option[JValue] = try {
    val imageName = StringHelpers.randomString(16)
    val tempFile = File.createTempFile(imageName, ".tmp")

    try {
      Files.write(bytes, tempFile)

      for {
        assetProvider <- findAssetProviders(showId)
        imageHandler <- ImageHandlers
      } {
        // generate images
        showcardProcessor.processDownloadedFile(assetProvider, tempFile, imageHandler, "file://" + tempFile.getAbsolutePath)

        // propagate the new showcard
        showcardProcessor.afterAssetTagsUpdated(assetProvider.roviProgramIds.is.head, assetProvider)
      }

      Some(("name" -> imageName) ~ ("type" -> "image/jpeg") ~ ("size" -> bytes.size) ~ ("success" -> true))
    } finally {
      tempFile.delete()
    }
  } catch {
    case ex: Exception => None
  }

  def findAssetProviders(showId: Int): List[AssetProvider] = {
    import com.foursquare.rogue.Rogue._
    AssetProvider where(_.yapShowId eqs showId) fetch()
  }
}
