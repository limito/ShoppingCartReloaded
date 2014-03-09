package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import me.limito.bukkit.shopcart.database.DatabaseScheduler

abstract class Request(val requestManager: RequestManager, val commandSender: CommandSender) {
  /** Игрок не может одновременно запускать несколько вопросов с mustLock=true */
  val mustLock = false

  /** Здесь идет первичная обработка запроса (в игровом потоке) **/
  def handle()

  def dao = requestManager.plugin.dao

  def withDatabase(f: () => Unit) {
    DatabaseScheduler.schedule(f)
  }

  def withBukkit(f: () => Unit) {
    requestManager.plugin.getServer.getScheduler.scheduleSyncDelayedTask(requestManager.plugin, new Runnable {
      def run() {f()}
    })
  }

  def requirePermission(permission: String) {
    if (!commandSender.hasPermission(permission))
      throw new NoPermissionException(permission)
  }

  def sendMessages(messages: Seq[String]) {
    val array = messages.toArray
    requestManager.plugin.getServer.getScheduler.scheduleSyncDelayedTask(requestManager.plugin, new Runnable {
      def run() {
        commandSender.sendMessage(array)
      }
    })
  }

  def sendMessage(message: String) {
    requestManager.plugin.getServer.getScheduler.scheduleSyncDelayedTask(requestManager.plugin, new Runnable {
      def run() {
        commandSender.sendMessage(message)
      }
    })
  }

  def completed() {
    requestManager.onCompleted(this)
  }

  def lang = requestManager.lang
}
