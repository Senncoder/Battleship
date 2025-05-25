package com.dosa.battleships

import android.os.Handler
import com.sun.net.httpserver.HttpExchange

class PingHandler : Handler() {
    override fun handleGet(httpExchange: HttpExchange, response: HandlerResponse) {
        response.statusCode = 200
        response.jsonOut["ping"] = true
    }
}