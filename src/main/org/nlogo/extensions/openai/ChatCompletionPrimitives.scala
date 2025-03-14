package org.nlogo.extensions.openai

import org.nlogo.api.{Argument, Context, ExtensionException, Reporter}
import org.nlogo.core.{LogoList, Syntax}
import org.nlogo.core.SyntaxJ
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.util.{Try, Success, Failure}

/**
 * Basic chat completion primitive
 * Usage: openai:chat-completion "gpt-3.5-turbo" "Hello, please introduce NetLogo."
 */
class ChatCompletion(ext: OpenAIExtension) extends Reporter {
  override def getSyntax: Syntax = {
    SyntaxJ.reporterSyntax(
      Array(Syntax.StringType, Syntax.StringType),
      Syntax.StringType
    )
  }

  override def report(args: Array[Argument], context: Context): AnyRef = {
    val model = args(0).getString
    val prompt = args(1).getString
    
    // Build chat messages
    val messages = List(Map("role" -> "user", "content" -> prompt))
    
    // Build request
    var body: Map[String, Any] = Map(
      "model" -> model,
      "messages" -> messages
    )
    
    // Add any stored options
    if (ext.chatOptions.nonEmpty) {
      body = body ++ ext.chatOptions.toMap
    }
    
    // Send request
    val response = HttpClient.post(ext, "chat/completions", body)
    
    // Parse response
    parseResponseContent(response)
  }
  
  // Parse response and extract content
  protected def parseResponseContent(response: String): String = {
    try {
      val json = parse(response)
      // Try to extract text from choices[0].message.content using proper JSON access
      val choices = (json \ "choices").asInstanceOf[JArray]
      if (choices.arr.isEmpty) {
        throw new ExtensionException("No choices found in API response")
      }
      val firstChoice = choices.arr.head
      val message = (firstChoice \ "message")
      val content = (message \ "content")
      
      content match {
        case JString(text) => text
        case _ => throw new ExtensionException("Could not extract content from API response")
      }
    } catch {
      case e: Exception => throw new ExtensionException(s"Failed to parse API response: ${e.getMessage}")
    }
  }
}

/**
 * Chat completion primitive with messages list
 * Usage: openai:chat-completion-with-messages "gpt-3.5-turbo" [["system" "You are a helpful assistant"] ["user" "Hello"]]
 */
class ChatCompletionWithMessages(ext: OpenAIExtension) extends Reporter {
  override def getSyntax: Syntax = {
    SyntaxJ.reporterSyntax(
      Array(Syntax.StringType, Syntax.ListType),
      Syntax.StringType
    )
  }

  override def report(args: Array[Argument], context: Context): AnyRef = {
    val model = args(0).getString
    val messagesList = args(1).getList
    
    // Convert message list
    val messages = convertMessagesToMaps(messagesList)
    
    // Build request
    var body: Map[String, Any] = Map(
      "model" -> model,
      "messages" -> messages
    )
    
    // Add any stored options
    if (ext.chatOptions.nonEmpty) {
      body = body ++ ext.chatOptions.toMap
    }
    
    // Send request
    val response = HttpClient.post(ext, "chat/completions", body)
    
    // Parse response
    parseResponseContent(response)
  }
  
  // Parse response and extract content
  private def parseResponseContent(response: String): String = {
    try {
      val json = parse(response)
      // Try to extract text from choices[0].message.content using proper JSON access
      val choices = (json \ "choices").asInstanceOf[JArray]
      if (choices.arr.isEmpty) {
        throw new ExtensionException("No choices found in API response")
      }
      val firstChoice = choices.arr.head
      val message = (firstChoice \ "message")
      val content = (message \ "content")
      
      content match {
        case JString(text) => text
        case _ => throw new ExtensionException("Could not extract content from API response")
      }
    } catch {
      case e: Exception => throw new ExtensionException(s"Failed to parse API response: ${e.getMessage}")
    }
  }
  
  // Convert NetLogo message list to Map format required by API
  private def convertMessagesToMaps(messagesList: LogoList): List[Map[String, String]] = {
    try {
      messagesList.scalaIterator.map { item =>
        val msgList = item.asInstanceOf[LogoList]
        if (msgList.size != 2) {
          throw new ExtensionException("Message format error: each message must be a list with two elements [role content]")
        }
        
        val role = msgList.get(0).toString
        val content = msgList.get(1).toString
        
        // Check if role is valid
        if (!List("system", "user", "assistant").contains(role)) {
          throw new ExtensionException(s"Invalid message role: $role. Must be 'system', 'user', or 'assistant'")
        }
        
        Map("role" -> role, "content" -> content)
      }.toList
    } catch {
      case e: ClassCastException => 
        throw new ExtensionException("Message list format error: each item must be a list")
      case e: ExtensionException => throw e
      case e: Exception => throw new ExtensionException(s"Error processing message list: ${e.getMessage}")
    }
  }
}

/**
 * Set chat option for chat completion primitives
 * Usage: openai:set-chat-option "temperature" 0.7
 *        openai:set-chat-option "max_tokens" 100
 */
class SetChatOption(ext: OpenAIExtension) extends org.nlogo.api.Command {
  override def getSyntax: Syntax = {
    SyntaxJ.commandSyntax(
      Array(Syntax.StringType, Syntax.WildcardType)
    )
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    val optionName = args(0).getString
    val optionValue = args(1).get
    
    // Process value based on type of option and option name
    val processedValue = optionName match {
      // Integer parameters
      case "max_tokens" | "n" | "logprobs" | "top_logprobs" | "presence_penalty" | "frequency_penalty" =>
        optionValue match {
          case n: java.lang.Double => n.intValue()
          case n: java.lang.Number => n.intValue()
          case s: String => s.toInt
          case _ => throw new ExtensionException(s"Option '$optionName' requires an integer value")
        }
      
      // Float parameters
      case "temperature" | "top_p" =>
        optionValue match {
          case n: java.lang.Double => n.doubleValue()
          case n: java.lang.Number => n.doubleValue()
          case s: String => s.toDouble
          case _ => throw new ExtensionException(s"Option '$optionName' requires a numeric value")
        }
      
      // Boolean parameters
      case "stream" | "echo" | "logit_bias" =>
        optionValue match {
          case b: java.lang.Boolean => b.booleanValue()
          case s: String => s.toLowerCase match {
            case "true" => true
            case "false" => false
            case _ => throw new ExtensionException(s"Option '$optionName' requires a boolean value (true/false)")
          }
          case _ => throw new ExtensionException(s"Option '$optionName' requires a boolean value")
        }
      
      // String parameters
      case "user" | "model" =>
        optionValue.toString
      
      // List or String parameters
      case "stop" =>
        optionValue match {
          case l: LogoList => l.scalaIterator.map(_.toString).toList
          case s: String => List(s)
          case _ => throw new ExtensionException(s"Option '$optionName' requires a list or string value")
        }
      
      // Default handling for unknown parameters
      case _ => 
        optionValue match {
          case n: java.lang.Double => n.doubleValue()
          case n: java.lang.Number => n
          case s: String => s
          case b: java.lang.Boolean => b.booleanValue()
          case l: LogoList => l.scalaIterator.map(_.toString).toList
          case _ => optionValue.toString
        }
    }
    
    // Store the option in the extension
    ext.chatOptions += (optionName -> processedValue)
  }
}

/**
 * Clear all chat options
 * Usage: openai:clear-chat-options
 */
class ClearChatOptions(ext: OpenAIExtension) extends org.nlogo.api.Command {
  override def getSyntax: Syntax = {
    SyntaxJ.commandSyntax(Array[Int]())
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    ext.chatOptions.clear()
  }
} 