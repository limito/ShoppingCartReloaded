package me.limito.bukkit.shopcart

import org.bukkit.command.CommandSender
import collection.mutable

/**
 * Задача: контроллировать запросы от игроков: не давать запускать сразу несколько запросов на запись в БД, защита от флуда и т.д.
 * Не является thread-safe
 */
class RequestManager(val plugin: ShoppingCartReloaded) {
  val locksMap = mutable.Map[CommandSender, Request]()

  def handleRequest(request: Request) {
    require(request.requestManager == this)

    if (checkLock(request))
      request.handle()
    else
      request.commandSender.sendMessage("Подождите, пока завершится предыдущий запрос")
  }

  def onCompleted(request: Request) {
    if (request.mustLock) {
      locksMap.remove(request.commandSender)
    }
  }

  private def checkLock(request: Request):Boolean = !locksMap.contains(request.commandSender)
}
