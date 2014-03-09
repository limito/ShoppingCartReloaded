package me.limito.bukkit.shopcart.request

import org.bukkit.command.CommandSender
import collection.mutable
import me.limito.bukkit.shopcart.ShoppingCartReloaded

/**
 * Задача: контроллировать запросы от игроков: не давать запускать сразу несколько запросов на запись в БД, защита от флуда и т.д.
 * Не является thread-safe
 */
class RequestManager(val plugin: ShoppingCartReloaded) {
  val locksMap = mutable.Map[CommandSender, Request]()
  def lang = plugin.lang

  def handleRequest(request: Request) {
    require(request.requestManager == this)

    if (checkLock(request)) {
      try {
        request.handle()
      } catch {
        case e: NoPermissionException => request.commandSender.sendMessage(lang.get("cart.no-perms"))
      }
    }
    else
      request.commandSender.sendMessage(lang.get("cart-get.try-later"))
  }

  def onCompleted(request: Request) {
    if (request.mustLock) {
      locksMap.remove(request.commandSender)
    }
  }

  private def checkLock(request: Request):Boolean = !locksMap.contains(request.commandSender)
}
