package org.nlogo.extensions.openai

import org.nlogo.api.ExtensionException
import scalaj.http.{Http, HttpOptions, HttpResponse}
import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.write

/**
 * HTTP client for making API requests
 */
object HttpClient {
  // Flag to indicate if we're in test mode
  private var testMode = false
  
  // Enable test mode (no actual API calls)
  def enableTestMode(): Unit = {
    testMode = true
  }
  
  // Disable test mode
  def disableTestMode(): Unit = {
    testMode = false
  }
  
  // Check if test mode is enabled
  def isTestMode: Boolean = testMode
  
  /**
   * Make a POST request to the API
   */
  def post(ext: OpenAIExtension, endpoint: String, body: Map[String, Any]): String = {
    // If in test mode, return mock responses
    if (testMode) {
      return getMockResponse(endpoint, body)
    }
    
    // Get API configuration
    val config = ext.getConfig
    val apiKey = ext.getApiKey.getOrElse(throw new ExtensionException("API key not set. Use openai:set-api-key to set it."))
    
    // Build URL
    val baseUrl = config("api_base")
    val url = s"$baseUrl/$endpoint"
    
    // Prepare headers
    val headers = scala.collection.mutable.Map[String, String](
      "Content-Type" -> "application/json"
    )
    
    // Add API key header based on API type
    if (config("api_type") == "azure") {
      headers += ("api-key" -> apiKey)
      
      // Add API version for Azure
      if (config("api_version").nonEmpty) {
        headers += ("api-version" -> config("api_version"))
      }
    } else {
      headers += ("Authorization" -> s"Bearer $apiKey")
    }
    
    // Convert body to JSON
    implicit val formats = DefaultFormats
    val jsonBody = write(body)
    
    try {
      // Make HTTP request
      val response: HttpResponse[String] = Http(url)
        .headers(headers.toMap)
        .postData(jsonBody)
        .option(HttpOptions.followRedirects(true))
        .option(HttpOptions.connTimeout(10000))
        .option(HttpOptions.readTimeout(50000))
        .asString
      
      // Check for errors
      if (response.code >= 400) {
        throw new ExtensionException(s"API request failed with status ${response.code}: ${response.body}")
      }
      
      response.body
    } catch {
      case e: Exception => throw new ExtensionException(s"API request failed: ${e.getMessage}")
    }
  }
  
  /**
   * Get mock responses for testing
   */
  private def getMockResponse(endpoint: String, body: Map[String, Any]): String = {
    endpoint match {
      case "chat/completions" => 
        """
        {
          "id": "chatcmpl-123",
          "object": "chat.completion",
          "created": 1677652288,
          "model": "gpt-3.5-turbo",
          "choices": [{
            "index": 0,
            "message": {
              "role": "assistant",
              "content": "This is a test response from the mock API."
            },
            "finish_reason": "stop"
          }]
        }
        """
      case "embeddings" =>
        """
        {
          "object": "list",
          "data": [{
            "object": "embedding",
            "embedding": [0.1, 0.2, 0.3, 0.4, 0.5],
            "index": 0
          }],
          "model": "text-embedding-ada-002"
        }
        """
      case _ => throw new ExtensionException(s"Unknown endpoint for mock response: $endpoint")
    }
  }
} 