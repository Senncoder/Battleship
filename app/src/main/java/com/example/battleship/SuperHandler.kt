package com.dosa.battleships

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter

/**
 * This class provides the top-level functionality for answering requests.
 * It is extended by classes serving specific paths.
 *
 * Note: The two segments of code dealing with CORS are only necessary if
 * we are dealing with web clients. Browsers will block cross-origin requests,
 * unless the server explicitly says that these are allowed.
 */
abstract class Handler : HttpHandler {

    @Throws(IOException::class)
    override fun handle(httpExchange: HttpExchange) {
        httpExchange.requestBody.bufferedReader().use { reader ->
            httpExchange.responseBody.writer().use { writer ->
                if (httpExchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                    httpExchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
                    httpExchange.sendResponseHeaders(204, -1) // No content for OPTIONS
                } else {
                    val response = HandlerResponse()

                    when (httpExchange.requestMethod.uppercase()) {
                        "GET" -> handleGet(httpExchange, response)
                        "POST" -> {
                            val jsonIn = readJSON(reader)
                            handlePost(httpExchange, jsonIn, response)
                        }
                        else -> {
                            response.statusCode = 418
                            response.jsonOut.put("Error", "Invalid HTTP request method")
                        }
                    }

                    httpExchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")

                    val textOut = response.jsonOut.toString()
                    httpExchange.sendResponseHeaders(response.statusCode, textOut.toByteArray().size.toLong())
                    writer.write(textOut)
                }
            }
        }
    }

    /**
     * The handler should override this method, if it supports GET-requests
     */
    protected open fun handleGet(httpExchange: HttpExchange, response: HandlerResponse) {
        response.statusCode = 418
        response.jsonOut.put("Error", "Invalid HTTP request method")
    }

    /**
     * The handler should override this method, if it supports POST-requests
     */
    protected open fun handlePost(httpExchange: HttpExchange, JSONin: JSONObject?, response: HandlerResponse) {
        response.statusCode = 418
        response.jsonOut.put("Error", "Invalid HTTP request method")
    }

    /**
     * Read the JSON from the input, and place into a JSONObject
     */
    protected fun readJSON(reader: BufferedReader): JSONObject? {
        return try {
            val jsonText = reader.readText()
            JSONObject(jsonText)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Helper method to read a JSON string. Getting exceptions is a nuisance - if a string does
     * not exist, just return null.
     */
    protected fun readString(obj: JSONObject, key: String): String? {
        return try {
            obj.getString(key)
        } catch (e: JSONException) {
            null
        }
    }
}