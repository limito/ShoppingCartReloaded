package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import me.limito.bukkit.shopcart.database.DatabaseScheduler
import java.util.logging.Level
import me.limito.bukkit.shopcart.ShoppingCartReloaded
import java.util.concurrent.CountDownLatch

abstract class Request(val commandSender: CommandSender) {
  /** Игрок не может одновременно запускать несколько вопросов с mustLock=true */
  val mustLock = false

  /** Здесь идет обработка запроса (в отдельном потоке) **/
  def handle()

  /** Здесь идет проверка условий для выполнения запроса (например, проверка наличия пермов) **/
  def prehandle() {}

  def dao = ShoppingCartReloaded.instance.dao

  def withBukkit(f: () => Unit) = {
    var throwable: Throwable = null
    val latch = new CountDownLatch(1)
    ShoppingCartReloaded.instance.getServer.getScheduler.scheduleSyncDelayedTask(ShoppingCartReloaded.instance.plugin, new Runnable {
      def run() {
        try {
          f()
        } catch {
          case t: Throwable => throwable = t
        }
        latch.countDown()
      }
    })
    latch.await()
    if (throwable != null) throw throwable
  }

  def requirePermission(permission: String) {
    if (!commandSender.hasPermission(permission))
      throw new NoPermissionException(permission)
  }

  def sendMessages(messages: Seq[String]) {
    if (ShoppingCartReloaded.instance.getServer.isPrimaryThread) {
      commandSender.sendMessage(messages.toArray)
    } else {
      val array = messages.toArray
      ShoppingCartReloaded.instance.getServer.getScheduler.scheduleSyncDelayedTask(ShoppingCartReloaded.instance.plugin, new Runnable {
        def run() {
          commandSender.sendMessage(array)
        }
      })
    }
  }

  def sendMessage(message: String) {
    if (ShoppingCartReloaded.instance.getServer.isPrimaryThread) {
      commandSender.sendMessage(message)
    } else {
      ShoppingCartReloaded.instance.getServer.getScheduler.scheduleSyncDelayedTask(ShoppingCartReloaded.instance.plugin, new Runnable {
        def run() {
          commandSender.sendMessage(message)
        }
      })
    }
  }

  def lang = ShoppingCartReloaded.instance.lang
}
