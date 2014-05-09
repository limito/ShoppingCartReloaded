package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.{ShoppingCartReloaded, Lang}
import org.bukkit.entity.Player
import me.limito.bukkit.shopcart.Lang.{TimeDuration, WorldName}
import ru.tehkode.permissions.bukkit.PermissionsEx
import org.bukkit.{Material, Bukkit}
import net.milkbowl.vault.permission.Permission
import org.bukkit.inventory.ItemStack

class CartItemPerm(permName: String, worldName: Option[String] = None) extends CartItem {
  val extras = worldName.map(WorldName).toList

  override def getYouGetMessage(amount: Int, lang: Lang): String = lang.format("cart-get.get-perm", getLocalizedName(lang))

  override def getLocalizedName(lang: Lang): String = lang.formatSubtypeExtra("perm", permName, extras)

  override def giveToPlayer(player: Player, amount: Int): Int = {
    val rsp = Bukkit.getServer.getServicesManager.getRegistration(classOf[Permission])
    if (rsp != null) {
      val permProvider = rsp.getProvider
      val worldNameOrNull = worldName.getOrElse(null)

      if (!permProvider.playerHas(worldNameOrNull, player.getName, permName)) {
        permProvider.playerAdd(worldNameOrNull, player.getName, permName)
        1
      } else {
        val lang = ShoppingCartReloaded.instance.lang
        player.sendMessage(ShoppingCartReloaded.instance.lang.format("cart-get.already-has-perm", getLocalizedName(lang)))
        0
      }
    } else 0
  }

  override def giveToPlayer(player: Player): Int = giveToPlayer(player, 1)

  override def getIcon: ItemStack = new ItemStack(Material.PAPER)
}
