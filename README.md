# NetLogo OpenAI Extension

A NetLogo extension that allows models to call OpenAI compatible APIs, such as OpenAI, Azure OpenAI, and other compatible services.

## Installation

Place this extension in the `extensions` directory of your NetLogo installation.

## Usage

First, import the extension in your NetLogo model:

```
extensions [openai]
```

### Setting API Key and Endpoints

Before using any functionality, you need to set the API key and endpoint:

```
openai:set-api-key "your-api-key"
openai:set-api-base "https://api.openai.com/v1"  ; Default OpenAI API endpoint
```

For Azure OpenAI:

```
openai:set-api-key "your-azure-api-key"
openai:set-api-base "https://your-resource-name.openai.azure.com"
openai:set-api-type "azure"
openai:set-api-version "2023-05-15"  ; Or other applicable API version
```

### Chat Completions

Basic usage:

```
let response openai:chat-completion "gpt-3.5-turbo" "Hello, please introduce NetLogo."
show response
```

Using multiple messages:

```
let messages (list 
  (list "system" "You are a helpful assistant.") 
  (list "user" "Please explain what agent-based modeling is.")
)
let response openai:chat-completion-with-messages "gpt-3.5-turbo" messages
show response
```

Setting options for chat completions:

```
; Set options that will apply to all chat completion calls
openai:set-chat-option "temperature" 0.7
openai:set-chat-option "max_tokens" 500

; Use with basic chat completion
let response1 openai:chat-completion "gpt-3.5-turbo" "Hello, please introduce NetLogo."

; Or use with messages
let messages (list 
  (list "system" "You are a helpful assistant.") 
  (list "user" "Please explain what agent-based modeling is.")
)
let response2 openai:chat-completion-with-messages "gpt-3.5-turbo" messages

; Clear options when no longer needed
openai:clear-chat-options
```

### Embeddings

Get text embeddings:

```
let embedding openai:get-embedding "text-embedding-ada-002" "NetLogo is an agent-based modeling environment."
show embedding
```

## Supported Features

- `openai:set-api-key` - Set the API key
- `openai:set-api-base` - Set the API base URL
- `openai:set-api-type` - Set the API type ("openai" or "azure")
- `openai:set-api-version` - Set the API version (mainly for Azure)
- `openai:chat-completion` - Basic chat completion with a single user message
- `openai:chat-completion-with-messages` - Chat completion with multiple messages
- `openai:set-chat-option` - Set an option for chat completions
- `openai:clear-chat-options` - Clear all chat completion options
- `openai:get-embedding` - Get text embeddings

## Examples

See NetLogo model examples in the `examples` directory.

## License

MIT License 