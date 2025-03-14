package org.nlogo.extensions.openai

import org.nlogo.api.{Argument, Command, Context, ExtensionException}
import org.nlogo.core.Syntax
import org.nlogo.core.SyntaxJ

/**
 * Set OpenAI API key
 */
class SetApiKey(ext: OpenAIExtension) extends Command {
  override def getSyntax: Syntax = {
    SyntaxJ.commandSyntax(Array(Syntax.StringType))
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    val key = args(0).getString
    if (key.trim.isEmpty) {
      throw new ExtensionException("API key cannot be empty")
    }
    ext.setApiKey(key)
  }
}

/**
 * Set API base URL
 */
class SetApiBase(ext: OpenAIExtension) extends Command {
  override def getSyntax: Syntax = {
    SyntaxJ.commandSyntax(Array(Syntax.StringType))
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    val base = args(0).getString
    if (base.trim.isEmpty) {
      throw new ExtensionException("API base URL cannot be empty")
    }
    ext.setConfig("api_base", base)
  }
}

/**
 * Set API type (openai or azure)
 */
class SetApiType(ext: OpenAIExtension) extends Command {
  override def getSyntax: Syntax = {
    SyntaxJ.commandSyntax(Array(Syntax.StringType))
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    val apiType = args(0).getString.toLowerCase
    if (apiType != "openai" && apiType != "azure") {
      throw new ExtensionException("API type must be 'openai' or 'azure'")
    }
    ext.setConfig("api_type", apiType)
  }
}

/**
 * Set API version (mainly used for Azure)
 */
class SetApiVersion(ext: OpenAIExtension) extends Command {
  override def getSyntax: Syntax = {
    SyntaxJ.commandSyntax(Array(Syntax.StringType))
  }

  override def perform(args: Array[Argument], context: Context): Unit = {
    val version = args(0).getString
    ext.setConfig("api_version", version)
  }
} 