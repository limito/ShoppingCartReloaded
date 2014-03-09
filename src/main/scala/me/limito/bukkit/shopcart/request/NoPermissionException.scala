package me.limito.bukkit.shopcart.request

class NoPermissionException(permissionName: String) extends RuntimeException {
  override def getMessage: String = permissionName
}
