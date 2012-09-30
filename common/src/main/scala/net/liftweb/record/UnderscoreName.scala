package net.liftweb.record

/**
 * Use this trait to automatically set the {@code name} of the field,
 * to the "fiendName" converted into the "underscore_notation", that should be used in mongo.
 * <b>Example:</b> Given a field with the name: "headendId" the name in mongo would become "headend_id".
 *
 * An usage example would be:
 * <code>
 *   object headendId extends StringField(this,100) { override def name = "headend_id" }
 * </code>
 *
 * can be replaced with:
 * <code>
 *   object headendId extends StringField(this,100) with UnderscoreName
 * </code>
 */
trait UnderscoreName extends BaseField {
  override lazy val name: String = UnderscoreName.fromCamelCase(fieldName)
}

object UnderscoreName {
  val UppercaseLetter = "([A-Z])".r

  def fromCamelCase(camelCase: String): String =
    if(camelCase == null) ""
    else camelCase.toCharArray.map(_.toString).map(_ match {
      case UppercaseLetter(l) => "_" + l.toLowerCase
      case "-" => "_"
      case s => s
    }).mkString
}