package faker

import language.implicitConversions

import util.Random

object Helper {

  /**
   * Accepts values like "de", "de-de", "de-ch". Transforms
   * value "de-de" to "de" and keeps "de-ch"
   */
  def locale(value: String) = {
    val prepValue = prepareLocale(value)
    if(prepValue.contains("-")) {
      val Array(lang, country, _*) = value.toLowerCase.split("-")
      if(lang == country) lang else value
    } else prepValue
  }

  private def prepareLocale(locale: String) = locale.toLowerCase.replaceAll("_", "-")

  /**
   * Gets the language from the locale string...
   */
  def language(locale: String) = prepareLocale(locale).substring(0, 2)

  def systemLocale = prepareLocale(java.util.Locale.getDefault.toString)

  def url(filename: String) = Option(getClass.getClassLoader.getResource(filename))

  implicit class RandomElementInCollection[T](orig: Traversable[T]) {
    def rand: T = orig.drop(Random.nextInt(orig.size)).head
  }
  implicit class RandomElementInArray[T](orig: Array[T]) {
    def rand: T = orig(Random.nextInt(orig.length))
  }
  implicit class RandomElementInSeq[T](orig: Seq[T]) {
    def rand: T = orig(Random.nextInt(orig.length))
  }
}
