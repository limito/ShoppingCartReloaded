package me.limito.bukkit.shopcart.database

import java.sql.Connection
import scala.collection.mutable

class NamedPrepStatement(connection: Connection, sql: String) {
  var name2index = mutable.Map[String, Int]()
  val placeholderSql: String = {
    def firstNotLetter(str: String, from: Int) = (from to sql.size).find(i => !Character.isLetter(sql.charAt(i))).getOrElse(sql.length)

    val builder = new StringBuilder()
    var i = 0
    var placeholdersNum = 0
    while (i < sql.length) {
      val c = sql.charAt(i)
      c match {
        case ':' =>
          val end = firstNotLetter(sql, i + 1)
          val name = sql.substring(i + 1, end)

          placeholdersNum = placeholdersNum + 1
          name2index += (name -> placeholdersNum)
          i = end
        case _ => builder += c; i = i + 1
      }
    }
    builder.toString()
  }

  val ps = connection.prepareStatement(placeholderSql)
}
