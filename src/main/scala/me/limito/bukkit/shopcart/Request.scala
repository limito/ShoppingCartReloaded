package me.limito.bukkit.shopcart

import org.bukkit.command.CommandSender

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
