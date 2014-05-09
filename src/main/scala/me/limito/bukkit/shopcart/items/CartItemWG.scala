package me.limito.bukkit.shopcart.items

import me.limito.bukkit.shopcart.{ShoppingCartReloaded, Lang}
import org.bukkit.entity.Player
import com.sk89q.worldguard.bukkit.{BukkitPlayer, WorldGuardPlugin}
import org.bukkit.plugin.Plugin
import org.bukkit.{Material, Bukkit}
import org.bukkit.inventory.ItemStack

sealed abstract class WGMembershipType
object WGMember extends WGMembershipType
object WGOwner extends WGMembershipType

class CartItemWG(regionName: String, membershipType: WGMembershipType, amount: Int) extends CartItem {
  override def getYouGetMessage(amount: Int, lang: Lang): String = membershipType match {
    case WGMember => lang.format("cart-get.get-rg-member", regionName)
    case WGOwner => lang.format("cart-get.get-rg-owner", regionName)
  }

  override def getLocalizedName(lang: Lang): String = membershipType match {
    case WGMember => lang.format("cart.region-mem", regionName)
    case WGOwner => lang.format("cart.region-own", regionName)
  }

  override def giveToPlayer(player: Player, amount: Int): Int = {
    val regionManager = getWorldGuard.getRegionManager(player.getWorld)
    val region = regionManager.getRegion(regionName)
    if (region != null) {
      val alreadyHasRegion = membershipType match {
        case WGOwner => region.getOwners.contains(player.getName)
        case WGMember => region.getOwners.contains(player.getName) || region.getMembers.contains(player.getName)
      }
      if (!alreadyHasRegion) {
        membershipType match {
          case WGOwner => region.getOwners.addPlayer(player.getName)
          case WGMember => region.getMembers.addPlayer(player.getName)
        }
        1
      } else 0
    } else {
      player.sendMessage(ShoppingCartReloaded.instance.lang.format("cart-get.no-wg-rg", regionName))
      0
    }
  }

  override def giveToPlayer(player: Player): Int = giveToPlayer(player, 1)

  def getWorldGuard: WorldGuardPlugin = {
    val plugin = Bukkit.getServer.getPluginManager.getPlugin("WorldGuard")

    plugin match {
      case p: WorldGuardPlugin => p
      case _ => throw new RuntimeException("No WG!")
    }
  }

  override def getIcon: ItemStack = new ItemStack(Material.WOOD_AXE)
}
