/*
 * Copyright 2007-2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package $package$

import java.io.File
import java.sql.{Connection, DriverManager}
import net.liftweb.db.ConnectionIdentifier
import net.liftweb.util.Props
import net.liftweb.db.DB
import net.liftweb.db.ConnectionManager
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.common.Empty
import net.liftweb.db.DefaultConnectionIdentifier
import net.liftweb.mapper.Schemifier


object DbProviders {
  def asList = PostgreSqlProvider :: MySqlProvider :: DerbyProvider :: H2FileProvider :: H2MemoryProvider :: Nil
  // Uncomment to run tests faster, but only against H2 def asList =  H2MemoryProvider :: Nil


  case object SnakeConnectionIdentifier extends ConnectionIdentifier {
    var jndiName = "snake"
  }

  trait Provider {
    def name: String
    def setupDB: Unit
    def required_? = Props.getBool(propsPrefix+"required", false)
    def propName: String
    lazy val propsPrefix = "mapper.test."+propName+"."
  }

  trait FileDbSetup {
    def filePath : String
    def vendor : Vendor

    def setupDB {
      val f = new File(filePath)

      def deleteIt(file: File) {
        if (file.exists) {
          if (file.isDirectory) file.listFiles.foreach{f => deleteIt(f)}
          file.delete
        }
      }

      // deleteIt(f)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      DB.defineConnectionManager(SnakeConnectionIdentifier, vendor)
    }
  }

  trait DbSetup {
    def vendor : Vendor

    def setupDB {
      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
      DB.defineConnectionManager(SnakeConnectionIdentifier, vendor)

      def deleteAllTables {
        DB.use(DefaultConnectionIdentifier) {
          conn =>
          val md = conn.getMetaData
          val rs = md.getTables(null, Schemifier.getDefaultSchemaName(conn), null, null)
          var toDelete: List[String] = Nil
          while (rs.next) {
            val tableName = rs.getString(3)
            if (rs.getString(4).toLowerCase == "table") toDelete = tableName :: toDelete
          }
          rs.close
        }
      }
      deleteAllTables
    }
  }

  abstract class Vendor(driverClass : String) extends ConnectionManager {
    def newConnection(name: ConnectionIdentifier): Box[Connection] = {
      Class.forName(driverClass)
      Full(mkConn)
    }

    def releaseConnection(conn: Connection) {
      try {
        conn.close
      } catch {
        case e: Throwable => Empty //ignore
      }
    }

    def mkConn : Connection
  }


  object MySqlProvider extends Provider with DbSetup {
    def name = "MySql"
    def vendor = new Vendor("com.mysql.jdbc.Driver") {
      def mkConn = {
        DriverManager.getConnection("jdbc:mysql://localhost:3306/lift_test?autoReconnect=true", "dpp", "")
      }
    }
    def propName: String = "mysql_local"
  }

  object PostgreSqlProvider extends Provider with DbSetup {
    def name = "PostgreSql"
    def vendor = new Vendor("org.postgresql.Driver") {
      def mkConn = DriverManager.getConnection("jdbc:postgresql://localhost/lift", "lift", "lift")
    }
    def propName: String = "psql_local"
  }

  object DerbyProvider extends Provider with FileDbSetup {
    def name = "Derby"
    def filePath = "target/tests_derby_lift"
    def vendor = new Vendor("org.apache.derby.jdbc.EmbeddedDriver") {
      def mkConn = DriverManager.getConnection("jdbc:derby:" + filePath + ";create=true")
    }
    def propName: String = "derby_local"
    override def required_? = true
  }

  object H2FileProvider extends Provider with FileDbSetup {
    def name = "H2"
    def filePath = "target/tests_h2_lift"
    def vendor = new Vendor("org.h2.Driver") {
      def mkConn = DriverManager.getConnection("jdbc:h2:" + filePath + "/test.db")
    }
    def propName: String = "hs_fs"
    override def required_? = true
  }

  object H2MemoryProvider extends Provider with DbSetup {
    def name = "H2 in memory"
    def vendor = new Vendor("org.h2.Driver") {
      def mkConn = DriverManager.getConnection("jdbc:h2:mem:lift;DB_CLOSE_DELAY=-1")
    }
    def propName: String = "hs_mem"
    override def required_? = true
  }

  object SqlServerProvider extends Provider with DbSetup {
    def name = "Microsoft SQL Server"
    def vendor = new Vendor("net.sourceforge.jtds.jdbc.Driver") {
      def mkConn = DriverManager.getConnection("jdbc:jtds:sqlserver://localhost/lift", "lift", "lift")
    }
    def propName: String = "ms_sqlserver"
  }

  object OracleProvider extends Provider with DbSetup {
    def name = "Oracle"
    def vendor = new Vendor("oracle.jdbc.OracleDriver") {
      def mkConn = DriverManager.getConnection("jdbc:oracle:thin:lift/lift@//localhost:1521/lift")
    }
    def propName: String = "oracle_local"
  }

  object MaxDbProvider extends Provider with DbSetup {
    def name = "SAP MaxDB"
    def vendor = new Vendor("com.sap.dbtech.jdbc.DriverSapDB") {
      def mkConn = DriverManager.getConnection("jdbc:sapdb://localhost:7210/lift?user=lift&password=lift")
    }
    def propName: String = "maxdb_local"
  }
}

