package com.dosa.battleships

import com.sun.net.httpserver.HttpExchange
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class HttpExchange(private val httpExchange: HttpExchange) {

    /**
     * Reads the input stream and returns the content as a JSONObject.
     */
    fun readJSON(): JSONObject? {
        return try {
            val `in` = BufferedReader(InputStreamReader(httpExchange.requestBody))
            val stringBuilder = StringBuilder()
            var line: String?
            while (`in`.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            JSONObject(stringBuilder.toString())
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Writes the response content to the output stream of the HttpExchange.
     */
    fun writeResponse(statusCode: Int, responseContent: String) {
        try {
            val out = OutputStreamWriter(httpExchange.responseBody)
            httpExchange.sendResponseHeaders(statusCode, responseContent.length.toLong())
            out.write(responseContent)
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Handles the CORS headers and sends a 204 response if the request is an OPTIONS request.
     * If not, it sends normal headers and content for a regular request.
     */
    fun handleCORS() {
        // Add CORS headers to the response
        httpExchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        httpExchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        httpExchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")

        // Handle OPTIONS request method for CORS pre-flight
        if (httpExchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
            try {
                httpExchange.sendResponseHeaders(204, -1) // No content for OPTIONS requests
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Retrieves the request method (GET, POST, etc.) for this HTTP request.
     */
    fun getRequestMethod(): String {
        return httpExchange.requestMethod
    }

    /**
     * Gets the request URI for this HTTP exchange.
     */
    fun getRequestURI(): String {
        return httpExchange.requestURI.toString()
    }
}