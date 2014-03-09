package me.limito.bukkit.shopcart.database

import java.util.concurrent.{Executors, ExecutorService}

object DatabaseScheduler {
  private val executors = Executors.newFixedThreadPool(2)

  def schedule(f: () => Unit) {
    executors.execute(new Runnable {
      def run() {f()}
    })
  }
}
