package org.nlogo.extensions.openai

import org.nlogo.api.{Argument, Context, ExtensionException, Reporter}
import org.nlogo.core.{LogoList, Syntax}
import org.nlogo.core.SyntaxJ
import org.json4s._
import org.json4s.native.JsonMethods._
import scala.collection.JavaConverters._

/**
 * Get text embedding primitive
 * Usage: openai:get-embedding "text-embedding-ada-002" "text to embed"
 */
class GetEmbedding(ext: OpenAIExtension) extends Reporter {
  override def getSyntax: Syntax = {
    SyntaxJ.reporterSyntax(
      Array(Syntax.StringType, Syntax.StringType),
      Syntax.ListType
    )
  }

  override def report(args: Array[Argument], context: Context): AnyRef = {
    val model = args(0).getString
    val text = args(1).getString
    
    // Build request
    val body = Map(
      "model" -> model,
      "input" -> text
    )
    
    // Send request
    val response = HttpClient.post(ext, "embeddings", body)
    
    // Parse response
    parseResponseEmbedding(response)
  }
  
  // Parse response and extract embedding vector
  private def parseResponseEmbedding(response: String): LogoList = {
    try {
      val json = parse(response)
      // Try to extract vector from data[0].embedding using proper JSON access
      val data = (json \ "data").asInstanceOf[JArray]
      if (data.arr.isEmpty) {
        throw new ExtensionException("No data found in API response")
      }
      val firstData = data.arr.head
      val embeddingJson = (firstData \ "embedding")
      
      embeddingJson match {
        case JArray(values) => 
          // Extract the double values from the JArray
          val embedding = values.map {
            case JDouble(d) => Double.box(d)
            case JInt(i) => Double.box(i.toDouble)
            case _ => throw new ExtensionException("Embedding vector contains non-numeric values")
          }.toList
          
          // Convert Scala List to Java List, then create LogoList
          LogoList.fromJava(embedding.asJava)
          
        case _ => throw new ExtensionException("Could not extract embedding vector from API response")
      }
    } catch {
      case e: Exception => throw new ExtensionException(s"Failed to parse API response: ${e.getMessage}")
    }
  }
} 