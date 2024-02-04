import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.memory.chat.ChatMemoryProvider
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.MemoryId
import dev.langchain4j.service.UserMessage
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import org.mapdb.DB
import org.mapdb.DBMaker

import dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson
import dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson
import org.mapdb.Serializer.INTEGER
import org.mapdb.Serializer.STRING
import java.{util => ju}
import ApiKeys.OPENAI_API_KEY.{value => apiKey}

trait Assistant:
  def chat(@MemoryId memoryId: Int, @UserMessage userMessage: String): String

class PersistentChatMemoryStore extends ChatMemoryStore:

  private val db  = DBMaker.fileDB("multi-user-chat-memory.db").transactionEnable().make()
  private val map = db.hashMap("messages", INTEGER, STRING).createOrOpen()

  override def getMessages(memoryId: Object): ju.List[ChatMessage] =
    messagesFromJson(map.get(memoryId))

  override def updateMessages(memoryId: Object, messages: ju.List[ChatMessage]): Unit =
    map.put(memoryId.asInstanceOf, messagesToJson(messages))
    db.commit()

  override def deleteMessages(memoryId: Object): Unit =
    map.remove(memoryId.asInstanceOf)
    db.commit()

object ServiceWithPersistentMemoryForEachUserExample:

  @main def differentMemories(): Unit =
    val store = new PersistentChatMemoryStore()

    val chatMemoryProvider: ChatMemoryProvider = memoryId =>
      MessageWindowChatMemory
        .builder()
        .id(memoryId)
        .maxMessages(10)
        .chatMemoryStore(store)
        .build()

    apiKey.foreach: key =>
      val assistant = AiServices
        .builder(classOf[Assistant])
        .chatLanguageModel(OpenAiChatModel.withApiKey(key))
        .chatMemoryProvider(chatMemoryProvider)
        .build()

      println(assistant.chat(1, "Hello, my name is Klaus"))
      println(assistant.chat(2, "Hi, my name is Francine"))

      // Now, comment out the two lines above, uncomment the two lines below, and run again.

    //   println(assistant.chat(1, "What is my name?"))
    //   println(assistant.chat(2, "What is my name?"))
