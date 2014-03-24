package me.limito.bukkit.shopcart.request

import me.limito.bukkit.shopcart.ShoppingCartReloaded
import java.util.concurrent.{ConcurrentHashMap, Executors}
import java.util.logging.Level

/**
 * Задача: контроллировать запросы от игроков: не давать запускать сразу несколько запросов на запись в БД, защита от флуда и т.д.
 * Не является thread-safe
 */
class RequestManager(val plugin: ShoppingCartReloaded) {
  val requestsExecutor = Executors.newFixedThreadPool(2)
  val playersRequestRunning = new ConcurrentHashMap[String, Request]()
  def lang = plugin.lang

  def handleRequest(request: Request) {
    if(playersRequestRunning.putIfAbsent(request.commandSender.getName, request) == null) {
      withExceptionHandling(request) {
        request.prehandle()
        requestsExecutor.submit(new Runnable {
          override def run() {
            withExceptionHandling(request) {
              request.handle()
            }
            onCompleted(request)
          }
        })
      }
    } else request.commandSender.sendMessage(lang.format("cart-get.try-later"))
  }

  def onCompleted(request: Request) {
    playersRequestRunning.remove(request.commandSender.getName)
  }

  def withExceptionHandling(request: Request)(f: => Unit) = {
    try {
      f
    } catch {
      case e: NoPermissionException => request.sendMessage(lang.get("cart.no-perms"))
      case e: Throwable =>
        request.sendMessage(lang.get("cart.error"))
        plugin.getLogger.log(Level.SEVERE, "Error completing request " + toString, e)
    }
  }

  def shutdown() {
    requestsExecutor.shutdownNow()
  }
}
