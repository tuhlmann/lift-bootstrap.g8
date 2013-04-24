package $package$.model

import net.liftweb._
import net.liftweb.common._
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.mapper.MetaProtoExtendedSession
import net.liftweb.mapper.ProtoExtendedSession
import net.liftweb.mapper.By

class ExtSession extends ProtoExtendedSession[ExtSession] {

  def getSingleton = ExtSession

}

/**
 * Extended sessions object to log user in automatically
 */
object ExtSession extends ExtSession with MetaProtoExtendedSession[ExtSession] with Loggable {
  override def dbTableName = "ext_session" // define the DB table name

  type UserType = User

  def logUserIdIn(uid: String): Unit = {
    tryo(uid.toLong).map(User.find(_).foreach(user => {
      userDidLogin(user)
//      if (user.rememberuser.is)
//        User.logUserIdIn(uid); //User.logUserIn(user) , commented because it lost session after second server restart
    }))
  }

  def recoverUserId: Box[String] = User.currentUserId

  // delete the ext cookie
  def deleteExtCookie() {
    for (cook <- S.findCookie(CookieName)) {
      S.deleteCookie(CookieName)
      logger.debug("S.deleteCookie called.")
      for {
        cv <- cook.value
        extSess <- find(By(cookieId, cv))
      } {
        extSess.delete_!
        logger.debug("ExtSession Record deleted.")
      }
    }
  }

  def handleExtSession: Box[ExtSession] = {
    val extSess = for {
      cookie <- S.findCookie(CookieName) // empty means we should ignore it
      cookieValue <- cookie.value ?~ "Cookie value is empty"
      es <- find(By(cookieId, cookieValue)) ?~ "ExtSession not found: %s".format(cookieValue)
    } yield es

    extSess match {
      case Failure(msg, _, _) => deleteExtCookie(); extSess // cookie is not valid, delete it
      case Full(es) if es.expiration.is < millis => // if it's expired, delete it and the cookie
        deleteExtCookie()
        Failure("Extended session has expired")
      case _ => extSess
    }
  }


}

/*
  def createExtSession(uid: String) {
    if (ObjectId.isValid(uid))
      createExtSession(new ObjectId(uid))
  }

  // delete the ext cookie
  def deleteExtCookie() {
    for (cook <- S.findCookie(cookieName)) {
      S.deleteCookie(cookieName)
      logger.debug("S.deleteCookie called.")
      for {
        cv <- cook.value
        uuid <- Helpers.tryo(UUID.fromString(cv))
        extSess <- find(uuid)
      } {
        extSess.delete_!
        logger.debug("ExtSession Record deleted.")
      }
    }
  }

}
*/
