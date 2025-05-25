package com.dosa.battleships


import org.json.JSONArray
import org.json.JSONObject

/**
 * This class represents the board for one player. While the playing board is
 * a 10x10 grid, we have no need to actually create such a grid. All relevant
 * calculations (hits, misses) are managed by the ships.
 */
class Board(shipsJSON: JSONArray) {

    private val ships: MutableMap<ShipType, Ship> = HashMap()

    init {
        for (i in 0 until shipsJSON.length()) {
            val shipJSON = shipsJSON.getJSONObject(i)
            val shipType = ShipType.valueOf(shipJSON.getString("ship"))
            val x = shipJSON.getInt("x")
            val y = shipJSON.getInt("y")
            val isHorizontal = shipJSON.getString("orientation") == "horizontal"
            val ship = Ship(shipType, x, y, isHorizontal)

            when {
                ships.containsKey(shipType) -> throw Exception("Duplicate ship type")
                ship.conflict(ships.values) -> throw Exception("Conflicting ship positions")
                else -> ships[shipType] = ship
            }
        }

        if (ships.keys.size != ShipType.values().size) {
            throw Exception("Missing ship types")
        }
    }

    fun hit(x: Int, y: Int): Boolean {
        return ships.values.any { it.hit(x, y) }
    }

    /**
     * Dynamically create list of ships that have been sunk
     */
    fun shipsSunk(): Array<ShipType> {
        return ships.values
            .filter { it.isDestroyed() }
            .map { it.getShipType() }
            .toTypedArray()
    }
}