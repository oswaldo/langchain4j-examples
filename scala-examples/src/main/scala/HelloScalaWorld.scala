import dev.langchain4j.model.openai.OpenAiChatModel
import ApiKeys.OPENAI_API_KEY.{value => apiKey}

@main def hello() = apiKey.foreach { key =>
  // Create an instance of a model
  val model = OpenAiChatModel.withApiKey(key)
  // Start interacting
  val answer = model.generate("Hello world!")
  println(answer) // Hello! How can I assist you today?
}
