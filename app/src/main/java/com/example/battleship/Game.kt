package com.dosa.battleships

import org.json.JSONArray
import java.time.Duration
import java.time.Instant
import java.util.Objects
import java.util.logging.Logger


class Game(val gamekey: String, player: String?, shipInfo: JSONArray?) {
    val players: ArrayList<String?> = ArrayList()
    private val boards = ArrayList<Board>()
    var playerToMove: String? = null // Waiting for this player (null if first-ever move)
        private set
    var isFinished: Boolean = false
        private set

    // Last shot fired (initially null)
    var lastX: Int? = null
        private set
    var lastY: Int? = null
        private set

    // Last time this game was used
    private var lastUse: Instant = Instant.now()

    init {
        players.add(player)
        boards.add(Board(shipInfo!!))
    }

    fun fire(x: Int, y: Int): Boolean {
        this.lastUse = Instant.now()
        val boardNumber = 1 - players.indexOf(playerToMove)
        playerToMove = players[1 - players.indexOf(playerToMove)] // swap players
        this.lastX = x
        this.lastY = y
        return (boards[boardNumber].hit(x, y))
    }

    /**
     * Get the ships sunk by a player, get their ship-type-names,
     * and put them into a JSONArray
     */
    fun getShipsSunkBy(player: String?): JSONArray {
        val boardNumber = 1 - players.indexOf(player)
        val shipList = JSONArray()
        val shipsSunk = boards[boardNumber].shipsSunk()

        // End of game detection !!
        this.isFinished = shipsSunk.size == 5

        for (shipType in shipsSunk) {
            shipList.put(shipType.toString())
        }
        return shipList
    }

    @Throws(Exception::class)
    fun addPlayer(player: String?, shipInfo: JSONArray?) {
        this.lastUse = Instant.now()
        players.add(player)
        boards.add(Board(shipInfo!!))

        // As soon as the second player has registered, choose who should
        // play first. This starts the game.
        playerToMove = players[(Math.random() * 2).toInt()]
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val game = o as Game
        return gamekey == game.gamekey
    }

    override fun hashCode(): Int {
        return Objects.hash(gamekey)
    }

    companion object {
        private val logger: Logger = Logger.getLogger("")
        private val games: MutableMap<String, Game> = HashMap()

        /**
         * All games that have not been used in 60 minutes are discarded.
         * To track this, any change to the game updates lastUse. This
         * means: adding a player or firing a shot.
         * We synchronize on the games collection, together with the
         * static add and get methods.
         */
        fun cleanupGames() {
            synchronized(games) {
                val currentTime = Instant.now()
                val keys = games.keys

                // Must use an Iterator, because we are modifying the collection (and map)
                val i = keys.iterator()
                while (i.hasNext()) {
                    val game = games[i.next()]
                    val duration = Duration.between(game!!.lastUse, currentTime)
                    if (duration.seconds > 3600) i.remove()
                }
            }
        }

        fun add(game: Game) {
            synchronized(games) {
                games.put(game.gamekey, game)
            }
        }

        fun get(gamekey: String): Game? {
            synchronized(games) {
                return games[gamekey]
            }
        }
    }
}
