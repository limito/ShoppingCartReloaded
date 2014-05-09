package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.{ShoppingCartReloaded, Lang}
import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.Lang.{TimeDuration, WorldName}
import ru.tehkode.permissions.bukkit.PermissionsEx
import org.bukkit.inventory.ItemStack
import org.bukkit.Material

class CartItemPermGroupPex(groupName: String, worldName: Option[String] = None, lifetime: Option[Long]) extends CartItem {
  val extras = (worldName.map(WorldName) ++ lifetime.map(TimeDuration)).toList

  override def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get-permgroup", getLocalizedName(lang))

  override def getLocalizedName(lang: Lang): String = lang.formatSubtypeExtra("perm-group", groupName, extras)

  override def giveToPlayer(player: Player, amount: Int): Int = {
    val worldNameOrNull = worldName.getOrElse(null)
    val user = PermissionsEx.getPermissionManager.getUser(player)

    if (user.getGroupsNames(worldNameOrNull).contains(groupName)) {
      val lang = ShoppingCartReloaded.instance.lang
      player.sendMessage(ShoppingCartReloaded.instance.lang.format("cart-get.already-in-perm-group", getLocalizedName(lang)))
      return 0
    }

    if (lifetime.isDefined)
      user.addGroup(groupName, worldNameOrNull, lifetime.get)
    else
      user.addGroup(groupName, worldNameOrNull)
    1
  }

  override def giveToPlayer(player: Player): Int = giveToPlayer(player, 1)

  override def getIcon: ItemStack = new ItemStack(Material.BOOK)
}
