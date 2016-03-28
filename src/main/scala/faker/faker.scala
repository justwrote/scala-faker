package faker

import org.yaml.snakeyaml.Yaml
import util.Random
import scala.collection.JavaConverters._
import java.util.ArrayList
import io.Source

import Helper._

object Faker {

  case class FakeData(locale: String) {

    private[this] val _language = url(filename(language(locale))) match {
      case Some(_) => language(locale)
      case _ => defaultLanguage
    }

    private[this] val _locale =
      if (_language == Helper.locale(locale)) None else Some(Helper.locale(locale))

    private[this] val (langData, localeData) = init(_language, _locale)

    private[Faker] def get(s: String): Option[Seq[AnyRef]] = _locale match {
      case Some(l) => get(s.replaceFirst("\\*", l) + ".", localeData) match {
                        case x: Some[_] => x
                        case _ => getFromLanguage(s)
                      }
      case _ => getFromLanguage(s)
    }

    private[this] def getFromLanguage(s: String) = get(s.replaceFirst("\\*", _language) + ".", langData)

    @scala.annotation.tailrec
    private[this] def get(s: String, data: Option[AnyRef]): Option[Seq[String]] = data match {
      case None => None
      case Some(x: java.util.Map[_, _]) if s.contains('.') =>
        val key = s.substring(0, s.indexOf('.'))
        val newKey = s.replaceFirst(key + ".", "")
        get(newKey, Option(x.asInstanceOf[java.util.Map[String, AnyRef]].get(key)))
      case Some(x: java.util.List[_]) if s.isEmpty =>
        Some(x.asInstanceOf[java.util.List[String]].asScala)
      case Some(x) => None
    }

    private[this] def loadFile(filename: String) = url(filename).map(Source.fromURL(_, "UTF-8"))

    private[this] def load(yaml: Yaml, locale: String) =
      loadFile(filename(locale))
        .map(x => yaml.load(x.mkString))
        .map(_.asInstanceOf[java.util.Map[String, AnyRef]])

    private def init(language: String, locale: Option[String]) = {
      val yaml = new Yaml
      val langData = load(yaml, language)
      val localeData = locale.flatMap(load(yaml, _))
      (langData, localeData)
    }
  }

  private[this] val defaultLanguage = "en"
  private[this] var data = FakeData(systemLocale)

  private[this] def filename(locale: String) = locale + ".yml"

  def locale(value: String) {
    synchronized {
      data = FakeData(value)
    }
  }

  private[faker] def get[T](s: String): Option[Seq[AnyRef]] = data.get(s)
}

trait Base {
  private val letters = 'a' to 'z'
  private val numerifyPattern = """#""".r
  private val letterifyPattern = """\?""".r

  def numerify(s: String): String =
    numerifyPattern.replaceAllIn(s, _ => Random.nextInt(10).toString)

  def letterify(s: String): String =
    letterifyPattern.replaceAllIn(s, _ => letters.rand.toString)

  // Nice name!
  def bothify(s: String): String =
    letterify(numerify(s))

  def fetch[T](key: String): T =
    Faker.get("*.faker." + key).map(_.rand).fold(null.asInstanceOf[T])(_.asInstanceOf[T])
}

object Internet extends Base {
  private val sep = List(".", "_", "")
  private val v4 = (2 to 255).toArray
  private val v6 = (0 to 65535).toArray
  private val nonWordPattern = """\W""".r

  def user_name: String = user_name("")

  def user_name(name: String): String =
    if(name.isEmpty) {
      if (Random.nextBoolean()) {
        nonWordPattern.replaceAllIn(Name.first_name, "").toLowerCase
      } else {
        List(Name.first_name, Name.last_name).
          map(x => nonWordPattern.replaceAllIn(x, "")).mkString(sep.rand).toLowerCase
      }
    } else {
      Random.shuffle(name.split(" ").toList).mkString(sep.rand).toLowerCase
    }

  def email: String = email("")
  def email(name: String): String = user_name(name) + "@" + domain_name

  def free_email: String = free_email("")
  def free_email(name: String): String = user_name(name) + "@" + fetch("internet.free_email")

  def domain_name: String = domain_word + "." + domain_suffix
  def domain_word: String = nonWordPattern.replaceAllIn(Company.name.split(" ").head, "").toLowerCase
  def domain_suffix: String = fetch("internet.domain_suffix")
  def ip_v4_address: String = {
    (1 to 4).map(_ => v4.rand).mkString(".")
  }
  def ip_v6_address: String = {
    (1 to 8).map(_ => v6.rand).map(x => "%x".format(x)).mkString(":")
  }
}

object Name extends Base {
  def name: String = fetch[java.util.ArrayList[String]]("name.formats").asScala.map(eval).mkString(" ")

  private def eval(s: String) = s match {
    case ":first_name" => first_name
    case ":last_name" => last_name
    case ":prefix" => prefix
    case ":suffix" => suffix
    case _ => ""
  }

  def first_name: String = fetch("name.first_name")
  def last_name: String = fetch("name.last_name")
  def prefix: String = fetch("name.prefix")
  def suffix: String = fetch("name.suffix")
}

object PhoneNumber extends Base {
  def phone_number: String = numerify(fetch("phone_number.formats"))
}

object Company extends Base {

  private def name1 = List(Name.last_name, suffix).mkString(" ")
  private def name2 = List(Name.last_name, Name.last_name).mkString("-")
  private def name3 = String.format("%s, %s and %s", Name.last_name, Name.last_name, Name.last_name)

  def name: String = List(name1 _, name2 _, name3 _).rand()
  def suffix: String = fetch("company.suffix")
}

object Lorem extends Base {

  private def rand(i: Int) = Random.nextInt(i)

  def words(num: Int = 3): List[String] =
    (1 until num).map(x => fetch[String]("lorem.words")).toList

  def sentence(word_count: Int = 4): String =
    words(word_count + rand(6)).mkString(" ").capitalize + "."

  def sentences(sentence_count: Int = 3): List[String] =
    (1 until sentence_count).map(x => sentence()).toList

  def paragraph(sentences_count: Int = 3): String =
    sentences(sentences_count + rand(3)).mkString(" ")

  def paragraphs(paragraph_count: Int = 3): List[String] =
    (1 until paragraph_count).map(x => paragraph()).toList
}

object Geo extends Base {

  type CoordsFun = () => (Double, Double)

  case class CoordsRange(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double) {
    val latRange = maxLat - minLat
    val lngRange = maxLng - minLng
  }

  private[faker] val EarthCoordsFun = coordsInArea(CoordsRange(-90, 90, -180, 180))

  def coords: (Double, Double) = EarthCoordsFun()

  def coordsInArea(range: CoordsRange): CoordsFun = new CoordsFun {
    import range._

    def apply() = (Random.nextDouble * latRange + minLat, Random.nextDouble * lngRange + minLng)
  }
}
