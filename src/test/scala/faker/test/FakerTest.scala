package faker.test

import faker._
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfterAll, WordSpec}

trait GeneralTest extends
  WordSpec with
  ShouldMatchers with
  BeforeAndAfterAll

class FakerTest extends GeneralTest {

  "Faker" should {
    "return a list of elements from the en.yml file" in {
      Faker.locale("en")
      val list = Faker.get("*.faker.address.city_prefix")
      list should be (Some(List("North", "East", "West", "South", "New", "Lake", "Port")))
    }

    "return german company suffixes" in {
      Faker.locale("de")
      val list = Faker.get("*.faker.company.suffix")
      list should be (Some(List("GmbH", "AG", "Gruppe")))
    }

    "return swiss company suffixes and german name suffix (testing fallback)" in {
      Faker.locale("de-ch")
      val list = Faker.get("*.faker.company.suffix")
      list should be (Some(List("AG", "GmbH", "und Söhne", "und Partner", "& Co.", "Gruppe", "LLC", "Inc.")))

      val list2 = Faker.get("*.faker.name.suffix")
      list2 should be (Some(List("von", "vom", "von der")))
    }
  }
}

class BaseTest extends GeneralTest {
  private val base = new Base {}

  override protected def beforeAll() {
    Faker.locale("en")
  }

  "Base" should {

    "numerify" in {
      base.numerify("###") should fullyMatch regex """\d{3}"""
      base.numerify("##########").toSeq.distinct.length should be > 1
    }

    "letterify" in {
      base.letterify("???") should fullyMatch regex """\w{3}"""
      base.letterify("??????????").toSeq.distinct.length should be > 1
    }

    "bothify" in {
      base.bothify("##??##") should fullyMatch regex """\d{2}\w{2}\d{2}"""
    }

    "return data if available" in {
      val data: String = base.fetch("address.city_prefix")
      data should (
        equal("North") or
        equal("East") or
        equal("West") or
        equal("South") or
        equal("New") or
        equal("Lake") or
        equal("Port"))
    }

    "return null if data is not available" in {
      val data: String = base.fetch("wrong")
      data should be (null)
    }
    
    "parse correctly from the base object" in {
      Faker.locale("xx")
      val data: String = Address.parse("address.test_parse")
      data should be ("Prefix")
    }

    "parse correctly two invocations from the base object" in {
      Faker.locale("xx")
      val data: String = Address.parse("address.test_parse2")
      data should be ("Prefix then follows Suffix")
    }

    "parse correctly from other object" in {
      Faker.locale("xx")
      val data: String = base.parse("other.test_parse3")
      data should be ("Prefix")
    }

    "parse correctly two invocations from other object" in {
      Faker.locale("xx")
      val data: String = base.parse("other.test_parse4")
      data should be ("Prefix then follows Suffix")
    }
  }
}

class NameTest extends GeneralTest {

  override protected def beforeAll() {
    Faker.locale("en")
  }

  "Name" should {

    "generate a valid name" in {
      Name.name should fullyMatch regex """([A-Za-z'\.]+ ?){2,3}"""
    }

    "name \"Julio O'Connell\" should be valid" in {
      "Julio O'Connell" should fullyMatch regex """([A-Za-z'\.]+ ?){2,3}"""
    }

    "name \"Camila O'Conner III\" should be valid" in {
      "Camila O'Conner III" should fullyMatch regex """([A-Za-z'\.]+ ?){2,3}"""
    }

    "generate a valid prefix" in {
      Name.prefix should fullyMatch regex """[A-Z][a-z]+\.?"""
    }

    "generate a valid suffix" in {
      Name.suffix should fullyMatch regex """[A-Z][A-Za-z]*\.?"""
    }
  }
}

class InternetTest extends GeneralTest {

  override protected def beforeAll() {
    Faker.locale("en")
  }

  "Internet" should {
    "generate a valid email address" in {
      Internet.email should fullyMatch regex """^[a-z0-9._%\-+]+@(?:[a-z0-9\-]+\.)+[a-z]{2,4}$"""
    }
    "generate a valid free email address" in {
      Internet.free_email should fullyMatch regex """.+@(gmail|hotmail|yahoo)\.com"""
    }
    "generate a username" in {
      Internet.user_name should fullyMatch regex """[a-z]+((_|\.)[a-z]+)?"""
    }
    "generate a valid username for a given name" in {
      Internet.user_name("bo peep") should fullyMatch regex """(bo(_|\.)?peep|peep(_|\.)?bo)"""
    }
    "generate a domain name" in {
      Internet.domain_name should fullyMatch regex """\w+\.\w+"""
    }
    "generate a domain world" in {
      Internet.domain_word should fullyMatch regex """^\w+$"""
    }
    "generate a domains suffix" in {
      Internet.domain_suffix should fullyMatch regex """^\w+(\.\w+)?"""
    }
    "generate an ipv4 address" in {
      val ip = Internet.ip_v4_address
      ip.toSeq.count(_ == '.') should be (3)
      (1 to 1000).foreach(x => {
        Internet.ip_v4_address.split('.').map(_.toInt).max should be <= 255
      })
    }
    "generate an ipv6 address" in {
      val ip = Internet.ip_v6_address
      ip.toSeq.count(_ == ':') should be (7)
      (1 to 1000).foreach(x => {
        Internet.ip_v6_address.split(':').map(Integer.valueOf(_, 16).intValue).max should be <= 65535
      })
    }
  }
}

class GeoTest extends GeneralTest {

  override protected def beforeAll() {
    Faker.locale("en")
  }

  def within(range: Geo.CoordsRange, coords: (Double, Double)): Boolean =
    range.maxLat >= coords._1 && coords._1 >= range.minLat && range.maxLng >= coords._2 && coords._2 >= range.minLng

  "Geo" should {

    "generate valid coordinates in a given range" in {
      val customRange = Geo.CoordsRange(-10, 10, -20, 20)
      val fn = Geo.coordsInArea(customRange)
      for(x <- 1 to 1000)
        within(customRange, fn())
    }
  }
}

class AddressTest extends GeneralTest {
  
  override protected def beforeAll() {
    Faker.locale("en")
  }
  
  "Address" should {
    "return a valid city" in {
      val data: String = Address.city
      data should fullyMatch regex """([A-Za-z'\.]+ ?){1,3}"""
    }   

    "return a valid street name" in {
      val data: String = Address.street_name
      data should fullyMatch regex """([A-Za-z'\.]+ ?){1,3}"""
    }   

    "return a valid street address" in {
      val data: String = Address.street_address(true)
      data should fullyMatch regex """[0-9]+ ([A-Za-z'\.]+ ?){1,3} [A-Za-z'\.]+ [0-9]+"""
    }   
  }

}