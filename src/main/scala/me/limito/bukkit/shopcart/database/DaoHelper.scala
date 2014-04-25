package me.limito.bukkit.shopcart.database

import com.j256.ormlite.field.DatabaseFieldConfig

trait DaoHelper {
  def fieldConfig(fieldName: String, columnName: String, nullable: Boolean): DatabaseFieldConfig = {
    val cfg = new DatabaseFieldConfig(fieldName)
    cfg.setColumnName(columnName)
    cfg.setCanBeNull(nullable)
    cfg
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
