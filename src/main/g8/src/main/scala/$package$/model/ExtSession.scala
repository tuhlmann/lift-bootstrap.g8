package $package$.model

import net.liftweb.common._
import net.liftmodules.mapperauth.model.MapperAuthExtSession
import net.liftweb.mapper.ProtoExtendedSession

/**
 * This stub needs to be implemented in the client project
 * FIXME: Remove dependency to implement in client project
 */
object ExtSession extends MapperAuthExtSession {

  type UserType = User

  def findUserByUniqueId(id: Long): Box[UserType] = User.find(id)

  def recoverUserId: Box[String] = User.currentUserId

}

//class ExtSession extends ProtoExtendedSession[ExtSession] {
//
//  def getSingleton = ExtSession
//
//}
