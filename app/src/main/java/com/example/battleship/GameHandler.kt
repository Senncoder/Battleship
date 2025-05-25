package com.dosa.battleships

import battleship.Game
import com.sun.net.httpserver.HttpExchange
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class GameHandler : Handler<Any?>() {

    override fun handlePost(httpExchange: HttpExchange, JSONin: JSONObject?, response: HandlerResponse) {
        val mapping = httpExchange.requestURI.toString() // For this handler, will begin with "/game"

        val player = readString(JSONin ?: JSONObject(), "player")
        val gamekey = readString(JSONin ?: JSONObject(), "gamekey")
        var game = Game.get(gamekey)

        if (player == null || gamekey == null) {
            response.jsonOut.put("Error", "Missing player or gamekey")
        } else {
            try {
                when (mapping) {
                    "/game/join" -> {
                        val shipArray = JSONin!!.getJSONArray("ships")
                        when {
                            player.length < 3 || gamekey.length < 3 -> {
                                response.jsonOut.put("Error", "Game-key or player too short")
                            }
                            game == null -> { // First player
                                game = Game(gamekey, player, shipArray)
                                Game.add(game)
                                sendGameStatus(game, player, response)
                            }
                            game.getPlayers().size > 1 -> {
                                response.jsonOut.put("Error", "Game already has two players")
                            }
                            player == game.getPlayers()[0] -> {
                                response.jsonOut.put("Error", "Duplicate player name")
                            }
                            else -> {
                                game.addPlayer(player, shipArray)
                                sendGameStatus(game, player, response)
                            }
                        }
                    }

                    "/game/fire" -> {
                        if (player == game?.getPlayerToMove()) {
                            try {
                                val x = JSONin!!.getInt("x")
                                val y = JSONin.getInt("y")
                                if (x !in 0..9 || y !in 0..9) throw JSONException("out of range")

                                val hit = game.fire(x, y)
                                val shipsSunk = game.getShipsSunkBy(player)
                                response.jsonOut.put("hit", hit)
                                response.jsonOut.put("shipsSunk", shipsSunk)
                            } catch (e: JSONException) {
                                response.jsonOut.put("Error", "Invalid move")
                            }
                        } else {
                            response.jsonOut.put("Error", "Not your turn")
                        }
                    }

                    "/game/enemyFire" -> {
                        sendGameStatus(game, player, response)
                    }

                    else -> {
                        throw Exception("No such mapping")
                    }
                }
            } catch (e: Exception) {
                response.jsonOut.put("Error", e.message)
            }
        }
    }

    /**
     * Wait for the opponent's move, then send the game status
     */
    private fun sendGameStatus(game: Game, player: String, response: HandlerResponse) {
        var sendStatus = true
        if (!game.isFinished()) {
            var ourTurn = false
            var count = 0
            while (!ourTurn && count < 600) {
                try {
                    count++
                    Thread.sleep(1000)
                } catch (ignored: InterruptedException) {
                }
                ourTurn = player == game.getPlayerToMove()
            }
            if (!ourTurn) {
                response.jsonOut.put("Error", "Timeout waiting for opponent")
                sendStatus = false
            }
        }
        if (sendStatus) {
            response.jsonOut.put("x", game.getLastX())
            response.jsonOut.put("y", game.getLastY())
            response.jsonOut.put("gameover", game.isFinished())
        }
    }
}