package me.limito.bukkit.shopcart

import net.minezrc.framework.test.CommandFramework.{CommandMessagedException, Command, CommandArgs}
import me.limito.bukkit.shopcart.request._
import org.bukkit.entity.Player

class Commands(pl: ShoppingCartReloaded) {
  def requestManager = pl.requestManager
  def lang = pl.lang

  @Command(name = "cart.get", permission = "cartr.user.get")
  def cartGet(args: CommandArgs) {
    val params = args.getArgs
    if (params.length >= 1) {
      val amount = if (params.length >= 2) parseNaturalInt(params(1)) else Int.MaxValue
      val req = new RequestItemGive(args.getSender, parseNaturalInt(params(0)), amount)
      requestManager.handleRequest(req)
    } else throw new CommandMessagedException(lang.get("cart.help"))
  }

  @Command(name = "cart.all", permission = "cartr.user.get")
  def cartAll(args: CommandArgs) {
    val req = new RequestGiveAll(args.getSender)
    requestManager.handleRequest(req)
  }

  @Command(name = "cart.gui", permission = "cartr.user.gui")
  def cartGui(args: CommandArgs) {
    requestManager.handleRequest(new RequestShowGui(args.getSender))
  }

  @Command(name = "cart.load", permission = "cartr.useradv.load")
  def cartLoad(args: CommandArgs) {
    requestManager.handleRequest(new RequestLoadItem(args.getSender, args.getSender.getName))
  }

  @Command(name = "cart.put", permission = "cartr.admin.put")
  def cartPut(args: CommandArgs) {
    val sender = args.getSender
    val params = args.getArgs
    if (sender.isInstanceOf[Player]) {
      val stack = args.getSender.asInstanceOf[Player].getItemInHand

      if (stack != null && stack.getTypeId > 0) {
        val owner = if (params.length >= 1) params(0) else sender.getName
        val amount = if (params.length >= 2) parseNaturalInt(params(1)) else stack.getAmount

        val req = new RequestPutItem(sender, owner, stack.clone(), amount)
        requestManager.handleRequest(req)
      } else sender.sendMessage(lang.get("cart-put.no-item"))
    } else sender.sendMessage(lang.get("misc.not-a-player"))
  }

  @Command(name = "cart", permission = "cartr.user.list")
  def cartList(args: CommandArgs) {
    if (args.getArgs.length == 0) {
      val req = new RequestItemsList(args.getSender)
      requestManager.handleRequest(req)
    } else args.getSender.sendMessage(lang.get("cart.help"))
  }

  private def parseNaturalInt(str: String): Int = {
    try {
      str.toInt
    }
    catch {
      case e: NumberFormatException => throw new CommandMessagedException(lang.get("misc.not-a-number"))
    }
  }
}
