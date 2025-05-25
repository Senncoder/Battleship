package com.dosa.battleships

import java.util.logging.Logger

class CleanupThread : Thread() {
    private val logger: Logger = Logger.getLogger("")

    init {
        name = "CleanupThread"
    }

    override fun run() {
        while (true) {
            logger.info("Cleanup process triggered")

            // Clean up games
            Game.cleanupGames()

            System.gc()

            // Log status
            val freeMemory = Runtime.getRuntime().freeMemory() / (1024 * 1024)
            logger.info("Cleanup process complete; $freeMemory MB available, ${Thread.activeCount()} threads running")

            try {
                Thread.sleep(300000) // Every 5 minutes
            } catch (ignored: InterruptedException) {
            }
        }
    }
}