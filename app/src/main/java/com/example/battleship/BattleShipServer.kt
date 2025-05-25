package com.dosa.battleships

import battleship.handlers.*
import com.sun.net.httpserver.HttpServer
import java.io.IOException
import java.net.InetSocketAddress
import java.util.concurrent.Executors
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger
import java.util.logging.SimpleFormatter



fun main(args: Array<String>) {


    val logger: Logger = Logger.getLogger("")
    var port = 50003


    setupLogging()
    try {
        if(args.isNotEmpty()) {
            logger.info("process command-line entry")
            val intValue = args[0].toInt()
            if (intValue in 1..65535) port = intValue
        }
        logger.info("Port is $port")

        // Start the clean-up thread: periodically delete accounts and chatrooms
        val ct = CleanupThread()
        ct.start()

        // Create the server and all valid mappings
        val server = HttpServer.create(InetSocketAddress(port), 0)
        server.createContext("/ping", PingHandler()) // ping with (POST) and without (GET) a token
        server.createContext("/game", GameHandler()) // game paths

        // If desired, use multiple threads for processing (here, with 4 threads)
        server.executor = Executors.newFixedThreadPool(4)

        // Start the server
        server.start()
    } catch (e: IOException) {
        logger.info(e.toString())
    }
}

private fun setupLogging() {

    val logger: Logger = Logger.getLogger("")

    logger.level = Level.FINE
    logger.handlers[0].level = Level.INFO // Standard (console) handler
    try {
        val fh = FileHandler("%h/BattleshipServer_%u_%g.log", 10000000, 2)
        fh.formatter = SimpleFormatter()
        fh.level = Level.FINE
        logger.addHandler(fh)
    } catch (e: Exception) {
        logger.severe("Unable to create file handler for logging: $e")
        throw RuntimeException("Unable to initialize log files: $e")
    }
}
