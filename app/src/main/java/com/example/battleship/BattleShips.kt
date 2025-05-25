package com.dosa.battleships


import java.util.*

class Ship(
    val shipType: ShipType,
    val x: Int,
    val y: Int,
    val isHorizontal: Boolean
) {
    private val hits: BooleanArray
    private var destroyed = false

    init {
        hits = BooleanArray(shipType.size)

        // Verify correctness of ship position
        if (x < 0 || y < 0 ||
            (isHorizontal && x + shipType.size > 10) ||
            (!isHorizontal && y + shipType.size > 10)
        ) {
            throw Exception("Invalid ship position")
        }
    }

    /**
     * Given a collection of ships, check whether this ship conflicts with
     * another ship (i.e., occupies squares that are already occupied).
     */
    fun conflict(ships: Collection<Ship>): Boolean {
        return ships.any { conflict(this, it) }
    }

    private fun conflict(ship1: Ship, ship2: Ship): Boolean {
        var conflict = false
        if (ship1.isHorizontal == ship2.isHorizontal) {
            if (ship1.isHorizontal && ship1.y == ship2.y) {
                conflict = linearConflict(ship1.x, ship1.shipType.size, ship2.x, ship2.shipType.size)
            } else if (!ship1.isHorizontal && ship1.x == ship2.x) {
                conflict = linearConflict(ship1.y, ship1.shipType.size, ship2.y, ship2.shipType.size)
            }
        } else { // Check for crossing
            val horizontalShip = if (ship1.isHorizontal) ship1 else ship2
            val verticalShip = if (!ship1.isHorizontal) ship1 else ship2
            val xMatch = horizontalShip.x <= verticalShip.x && (horizontalShip.x + horizontalShip.shipType.size - 1) >= verticalShip.x
            val yMatch = verticalShip.y <= horizontalShip.y && (verticalShip.y + verticalShip.shipType.size - 1) >= horizontalShip.y
            conflict = xMatch && yMatch
        }
        if (conflict) {
            println("${ship1.shipType} ${ship2.shipType}")
        }
        return conflict
    }

    private fun linearConflict(min1: Int, size1: Int, min2: Int, size2: Int): Boolean {
        val max1 = min1 + size1 - 1
        val max2 = min2 + size2 - 1
        return max1 > min2 && max2 > min1
    }

    fun hit(x: Int, y: Int): Boolean {
        val hitPart = if (isHorizontal) {
            hitsShipPart(x, y, this.x, this.y, this.shipType.size)
        } else { // isVertical
            hitsShipPart(y, x, this.y, this.x, this.shipType.size)
        }
        return hitPart >= 0
    }

    /**
     * Return an int indicating which part of the ship has been hit.
     * This reflects the array `hits`
     */
    private fun hitsShipPart(s1: Int, s2: Int, c1: Int, c2: Int, size: Int): Int {
        return if (s2 == c2 && s1 in c1..(c1 + size - 1)) {
            hits[s1 - c1] = true
            destroyed = checkIsDestroyed()
            s1 - c1
        } else { // not a hit
            -1
        }
    }

    private fun checkIsDestroyed(): Boolean {
        return hits.all { it }
    }

    fun isDestroyed(): Boolean {
        return destroyed
    }
}