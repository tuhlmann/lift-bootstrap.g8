package $package$.model

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.util.Helpers._
import net.liftweb.mapper.By
import net.liftweb.mapper.MappedLongForeignKey
import $package$.model.share.MappedExpiringDate
import $package$.lib.AppConfig

/**
  * This is a token for automatically logging a user in
  */
class LoginToken extends LongKeyedMapper[LoginToken] with IdPK {
  def getSingleton = LoginToken

  object userId extends MappedLongForeignKey(this, User)
  object expires extends MappedExpiringDate(this, getSingleton.loginTokenExpires)

  def url: String = getSingleton.url(this)
}


object LoginToken extends LoginToken with LongKeyedMetaMapper[LoginToken] {

  //override def dbTableName = "logintokens"

  //ensureIndex((userId.name -> 1))

  private lazy val loginTokenUrl = AppConfig.loginTokenUrl.vend
  private lazy val loginTokenExpires = AppConfig.loginTokenExpires.vend

  def url(inst: LoginToken): String = "%s%s?token=%s".format(S.hostAndPath, loginTokenUrl, inst.id.toString)

  def createForUserId(uid: Long): LoginToken = {
    create.userId(uid).saveMe
  }

  def deleteAllByUserId(uid: Long) {
    this.find(By(userId, uid)).foreach(_.delete_!)
  }

  def findByStringId(in: String): Box[LoginToken] =
    try {
      find(By(LoginToken.userId, in.toLong))
    } catch {
      case e: Exception => Failure("Invalid Id: "+in)
    }

//    if (ObjectId.isValid(in)) find(new ObjectId(in))
//    else Failure("Invalid ObjectId: "+in)
}
