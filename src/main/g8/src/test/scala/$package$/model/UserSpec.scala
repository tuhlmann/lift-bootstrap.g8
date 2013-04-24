package $package$.model
import $package$.DbProviders
import $package$.BaseSpec
import $package$.MapperSpecsModel
import org.scalatest.BeforeAndAfterAll

class UserSpec extends BaseSpec with BeforeAndAfterAll {

  MapperSpecsModel.setup()

  def provider = DbProviders.H2MemoryProvider
  def doLog = false
  private def ignoreLogger(f: => AnyRef): Unit = ()

  override def beforeAll = {
    MapperSpecsModel.cleanup
  }

  provider.setupDB

  "User with " + provider.name should {
    "create, validate, save, and retrieve properly" in {

      val userPass = "testpass1"
      // create a new User instance
      val newUser = User.create.email("test@liftweb.net")

      newUser.password(userPass)

      val errs = newUser.validate
      if (errs.length > 1) {
        fail("Validation error: " + errs.mkString(", "))
      }

      newUser.name("Test")
      newUser.username("Test")
      newUser.validate.length should equal(0)

      // save to db
      //newUser.password.hashIt
      newUser.save

      // retrieve from db and compare
      val userFromDb = User.find(newUser.id.is)
      userFromDb.isDefined should equal(true)
      userFromDb.map(u => u.id.is should equal(newUser.id.is))
    }

    "Support password properly" in {

      val userPass = "testpass2"
      // create a new User instance
      val newUser = User.create.email("test2@liftweb.net").name("Test2").username("Test2").password(userPass)

      // check password
      newUser.password.match_?("xxxxx") should equal(false)
      newUser.password.match_?(userPass) should equal(true)

      newUser.validate.length should equal(0)

      // save to db
      newUser.save

      // retrieve from db and compare
      val userFromDb = User.find(newUser.id.is)

      userFromDb.isDefined should equal(true)
      userFromDb.map(u => {
        u.id.is should equal(newUser.id.is)
        u.password.match_?("xxxxx") should equal(false)
        u.password.match_?(userPass) should equal(true)
      })
    }
  }

}
