package com.dosa.battleships


import org.json.JSONObject

class HandlerResponse {
    var statusCode: Int = 200 // Optimistic default
    val jsonOut: JSONObject = JSONObject()
}