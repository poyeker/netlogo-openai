package org.nlogo.extensions.openai

import org.nlogo.api.{DefaultClassManager, PrimitiveManager, ExtensionException, ExtensionManager}
import scala.collection.mutable.{Map => MutableMap}

/**
 * Main class for the OpenAI extension, used to interact with OpenAI compatible APIs
 */
class OpenAIExtension extends DefaultClassManager {
  // Store API configuration
  private val config = MutableMap[String, String](
    "api_type" -> "openai",
    "api_base" -> "https://api.openai.com/v1",
    "api_version" -> ""
  )
  
  // API key (stored separately for better security)
  private var apiKey: Option[String] = None
  
  // Store chat completion options
  val chatOptions = MutableMap[String, Any]()
  
  // Get API configuration
  def getConfig: Map[String, String] = config.toMap
  
  // Get API key
  def getApiKey: Option[String] = apiKey
  
  // Set API key
  def setApiKey(key: String): Unit = {
    apiKey = Some(key)
  }
  
  // Set configuration item
  def setConfig(key: String, value: String): Unit = {
    config(key) = value
  }
  
  /**
   * Load extension and register primitives
   */
  override def load(primitiveManager: PrimitiveManager): Unit = {
    // Configuration primitives
    primitiveManager.addPrimitive("set-api-key", new SetApiKey(this))
    primitiveManager.addPrimitive("set-api-base", new SetApiBase(this))
    primitiveManager.addPrimitive("set-api-type", new SetApiType(this))
    primitiveManager.addPrimitive("set-api-version", new SetApiVersion(this))
    
    // Chat completion primitives
    primitiveManager.addPrimitive("chat-completion", new ChatCompletion(this))
    primitiveManager.addPrimitive("chat-completion-with-messages", new ChatCompletionWithMessages(this))
    primitiveManager.addPrimitive("set-chat-option", new SetChatOption(this))
    primitiveManager.addPrimitive("clear-chat-options", new ClearChatOptions(this))
    
    // Embedding primitives
    primitiveManager.addPrimitive("get-embedding", new GetEmbedding(this))
    
    // Enable test mode if running tests
    if (System.getProperty("org.nlogo.preferHeadless", "false").toBoolean) {
      HttpClient.enableTestMode()
    }
  }
  
  /**
   * Cleanup method, called when extension is unloaded
   */
  override def unload(em: ExtensionManager): Unit = {
    // Clear API key and configuration
    apiKey = None
    config.clear()
    chatOptions.clear()
    
    // Disable test mode
    HttpClient.disableTestMode()
  }
} 