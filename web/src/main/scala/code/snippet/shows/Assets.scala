package code.snippet.shows

import net.liftweb.http.{S, SHtml}
import tv.yap.model.messaging.{YapAsset, AssetProvider}
import xml.{NodeSeq, Elem, Text}
import net.liftweb.util._
import Helpers._
import org.joda.time.DateTime

class Assets {
  def ellipsis(s:String) =
    if(s.length>40) s.substring(0,19)+"..." else s

  def renderAssets(assets:List[YapAsset],title:String,operations:(YapAsset)=>Elem): NodeSeq = {
    val summary = "'%s': %d assets ".format(title,assets.length)
    <tr><td colspan="4" style="font-weight:bold">{summary}</td></tr> ::
    assets.map(asset=>{
      <tr>
        <td><img  style="width:100px" src={asset.url.is}/></td>
        <td>{ellipsis(asset.caption.is)}</td>
        <td>{asset.endAt.is.map(_.getTime).getOrElse("-")}</td>
        <td>{operations(asset)}</td>
      </tr>
    })
  }


  def activateAsset(asset: YapAsset, assetProvider: AssetProvider): Elem = {
    SHtml.a(() => if (asset.activeAt(new DateTime())) assetProvider.expireAsset(asset) else assetProvider.activateAsset(asset), if (asset.activeAt(new DateTime())) Text("expire") else Text("activate"))
  }

  def activateAndMoveAsset(asset:YapAsset,assetProvider:AssetProvider):Elem = {
    val activate = activateAsset(asset,assetProvider)
    val move = SHtml.a(() => assetProvider.moveToUsaNetworkAssets(asset), Text("To USA"))
    <span>{activate} | {move}</span>
  }

  def render = {
    AssetProvider.byYapShowId(S.param("yap_show_id").get.toInt).map(assetProvider => {
            "#asset_container" #> {
              renderAssets(assetProvider.yapAssets.is, "Yap", activateAndMoveAsset(_, assetProvider)) ++
              renderAssets(assetProvider.usaNetworkAssets.is, "USA Network", activateAsset(_, assetProvider)) ++
              renderAssets(assetProvider.facebookAssets.is, "Facebook", activateAsset(_, assetProvider)) ++
              renderAssets(assetProvider.tmsAssets.is, "TMS", activateAsset(_, assetProvider)) ++
              renderAssets(assetProvider.roviAssets.is, "Rovi", activateAsset(_, assetProvider))
            }
    }).getOrElse("#asset_container" #> "No assets")
  }

}
