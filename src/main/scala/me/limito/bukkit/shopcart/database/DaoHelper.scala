package me.limito.bukkit.shopcart.database

import com.j256.ormlite.field.DatabaseFieldConfig
import java.sql.Connection

trait DaoHelper {
  def fieldConfig(fieldName: String, columnName: String, nullable: Boolean = true, defaultValue: Option[String] = None, indexed: Boolean = false): Option[DatabaseFieldConfig] = {
    if (columnName != "-") {
      val cfg = new DatabaseFieldConfig(fieldName)
      cfg.setColumnName(columnName)
      cfg.setCanBeNull(nullable)
      cfg.setIndex(indexed)
      defaultValue.foreach(cfg.setDefaultValue)
      Some(cfg)
    } else {
      None
    }
  }

  def idConfig(fieldName: String, columnName: String): DatabaseFieldConfig = {
    val cfg = new DatabaseFieldConfig(fieldName)
    cfg.setColumnName(columnName)
    cfg.setCanBeNull(false)
    cfg.setGeneratedId(true)
    cfg.setAllowGeneratedIdInsert(true)
    cfg
  }

  def withExceptionHandling[T](action: => T): T = {
    try {
      action
    } catch {
      case ex: Exception => throw new DaoException(ex)
    }
  }
}
