package $package$.model

import net.liftweb._
import net.liftweb.mapper.MappedStringIndex
import net.liftweb.mapper.LongKeyedMapper
import net.liftweb.mapper.LongKeyedMetaMapper
import net.liftweb.mapper.IdPK
import net.liftweb.mapper.MappedString
import net.liftweb.mapper.MappedStringForeignKey
import net.liftweb.mapper.MappedLongForeignKey
import $package$.lib.APermission
import $package$.model.share.UserId


/*
 * Simple record for storing permissions. Permission name is the PK.
 */
object Permission extends Permission with LongKeyedMetaMapper[Permission]  {

  def toAPermission(perm: Permission) = APermission.fromString(perm.permission.is)
  def fromAPermission(aPerm: APermission): Permission = Permission.create.permission(aPerm.toString)

  def userPermissions(uid: Long): List[APermission] = userId.findFor(uid).map(toAPermission)


//  def findOrCreate(in: String): Permission = find(in).openOr(create.id(in))
//  def findOrCreateAndSave(in: String): Permission = findOrCreate(in).saveMe

}

class Permission extends LongKeyedMapper[Permission] with UserId[Permission] with IdPK {
  def getSingleton = Permission

  object roleId extends MappedStringForeignKey(this, Role, 32) {
    def foreignMeta = Role
  }

  object permission extends MappedString(this, 1024)


}

