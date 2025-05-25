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
 * Note: The two segments of code dealing with CORS are only necessary, if
 * we are dealing with web clients. Browsers will block cross-origin requests,
 * unless the server explicitly says that these are allowed.
 */
abstract class Handler<HttpHandler> : HttpHandler {

    @Throws(IOException::class)
    override fun handle(httpExchange: HttpExchange) {
        BufferedReader(InputStreamReader(httpExchange.requestBody)).use { `in` ->
            OutputStreamWriter(httpExchange.responseBody).use { out ->
                // Web clients are sending cross-origin, because the client is not running on this server.
                // In that case, the browser sends a pre-flight request, to ensure that a cross-origin
                // request will be accepted. This is an OPTIONS command, and must be answered with headers
                // that show what cross-origin commands are acceptable.
                if (httpExchange.requestMethod.equals("OPTIONS", ignoreCase = true)) {
                    httpExchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
                    httpExchange.sendResponseHeaders(204, -1) // No content for OPTIONS requests
                } else { // For all other requests, our usual processing
                    val response = HandlerResponse()
                    val requestMethod = httpExchange.requestMethod

                    when (requestMethod) {
                        "GET" -> handleGet(httpExchange, response)
                        "POST" -> {
                            val jsonIn = readJSON(`in`)
                            handlePost(httpExchange, jsonIn, response)
                        }
                        else -> { // Unsupported request type
                            response.statusCode = 418
                            response.jsonOut.put("Error", "Invalid HTTP request method")
                        }
                    }

                    // We include the CORS headers for all normal requests as well,
                    // to ensure that web clients are happy.
                    httpExchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
                    httpExchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")

                    // Send the response
                    val textOut = response.jsonOut.toString()
                    httpExchange.sendResponseHeaders(response.statusCode, textOut.length.toLong())
                    out.write(textOut)
                }
            }
        }
    }

    /**
     * The handler should override this method if it supports GET-requests
     */
    protected open fun handleGet(httpExchange: HttpExchange, response: HandlerResponse) {
        response.statusCode = 418
        response.jsonOut.put("Error", "Invalid HTTP request method")
    }

    /**
     * The handler should override this method if it supports POST-requests
     */
    protected open fun handlePost(httpExchange: HttpExchange, jsonIn: JSONObject?, response: HandlerResponse) {
        response.statusCode = 418
        response.jsonOut.put("Error", "Invalid HTTP request method")
    }

    /**
     * Read the JSON from the input and place it into a JSONObject
     */
    protected fun readJSON(`in`: BufferedReader): JSONObject? {
        return try {
            val stringBuilder = StringBuilder()
            var line: String?
            while (`in`.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }
            JSONObject(stringBuilder.toString())
        } catch (e: Exception) {
            // If anything goes wrong, return null
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

