package code.presenter

import xml._

trait StyleablePresenter {

  import StyleablePresenter._

  implicit def nodeSeq2Styles(n: NodeSeq) = new StyleableNodeSeq(n)

}

object StyleablePresenter {

  class StyleableNodeSeq(nodes: NodeSeq) {

    def styledStrong: NodeSeq = styled("strong")

    def styledEm: NodeSeq = styled("em")
    def styledEmphasized: NodeSeq = styled("em")

    def styled(className:String): NodeSeq = nodes.seq map { _ match {
        case el: Elem => el % Attribute(None, "class", Text(className), Null)
        case el => el
      }
    }
  }
}