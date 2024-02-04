import dev.langchain4j.chain.ConversationalRetrievalChain
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentSplitter
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.chat.ChatLanguageModel
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.input.Prompt
import dev.langchain4j.model.input.PromptTemplate
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiTokenizer
import dev.langchain4j.retriever.EmbeddingStoreRetriever
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.model.openai.OpenAiModelName.GPT_3_5_TURBO
import os._
import ApiKeys.OPENAI_API_KEY.{value => apiKey}
import scala.jdk.CollectionConverters._
import scala.jdk.DurationConverters._
import scala.concurrent.duration.*

object ChatWithDocumentsExamples:

  @main def ifYouNeedSimplicity() =
    val embeddingModel = new AllMiniLmL6V2EmbeddingModel()
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]()
    val ingestor = EmbeddingStoreIngestor
      .builder()
      .documentSplitter(DocumentSplitters.recursive(300, 0))
      .embeddingModel(embeddingModel)
      .embeddingStore(embeddingStore)
      .build()
    val happyCarrotTxt = toPath("other-examples/src/main/resources/example-files/story-about-happy-carrot.txt")
    val document =
      loadDocument(happyCarrotTxt, new TextDocumentParser())
    ingestor.ingest(document)
    apiKey.foreach: key =>
      val chain = ConversationalRetrievalChain
        .builder()
        .chatLanguageModel(OpenAiChatModel.withApiKey(key))
        .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
        // .chatMemory() // you can override default chat memory
        // .promptTemplate() // you can override default prompt template
        .build()
      val answer = chain.execute("Who is Charlie?")
      println(answer) // Charlie is a cheerful carrot living in VeggieVille...

  @main def ifYouNeedMoreControl() =
    val happyCarrotTxt = toPath("other-examples/src/main/resources/example-files/story-about-happy-carrot.txt")
    // Load the document that includes the information you'd like to "chat" about with the model.
    val document = loadDocument(happyCarrotTxt, new TextDocumentParser())
    // Split document into segments 100 tokens each
    val splitter = DocumentSplitters.recursive(100, 0, new OpenAiTokenizer(GPT_3_5_TURBO))
    val segments = splitter.split(document)
    // Embed segments (convert them into vectors that represent the meaning) using embedding model
    val embeddingModel = new AllMiniLmL6V2EmbeddingModel()
    val embeddings     = embeddingModel.embedAll(segments).content()
    // Store embeddings into embedding store for further search / retrieval
    val embeddingStore = new InMemoryEmbeddingStore[TextSegment]()
    embeddingStore.addAll(embeddings, segments)
    // Specify the question you want to ask the model
    val question = "Who is Charlie?"
    // Embed the question
    val questionEmbedding = embeddingModel.embed(question).content()
    // Find relevant embeddings in embedding store by semantic similarity
    // You can play with parameters below to find a sweet spot for your specific use case
    val maxResults         = 3
    val minScore           = 0.7
    val relevantEmbeddings = embeddingStore.findRelevant(questionEmbedding, maxResults, minScore)
    // Create a prompt for the model that includes question and relevant embeddings
    val promptTemplate = PromptTemplate.from(
      """|Answer the following question to the best of your ability:
         |
         |Question:
         |{{question}}
         |
         |Base your answer on the following information:
         |{{information}}""".stripMargin,
    )
    val information = relevantEmbeddings.asScala
      .map(_.embedded().text())
      .mkString("\n" * 2)
    val variables = Map(
      "question"    -> question,
      "information" -> information,
    )
    val prompt = promptTemplate.apply(variables.asJava)
    apiKey.foreach: key =>
      // Send the prompt to the OpenAI chat model
      val chatModel = OpenAiChatModel
        .builder()
        .apiKey(key)
        .timeout(60.seconds.toJava)
        .build()
      val aiMessage = chatModel.generate(prompt.toUserMessage()).content()
      // See an answer from the model
      val answer = aiMessage.text()
      println(answer) // Charlie is a cheerful carrot living in VeggieVille...

  def toPath(fileName: String): java.nio.file.Path =
    (
      // assuming you are running this from your local clone called "langchain4j-examples" or from the "scala-examples" subdirectory for now, avoiding some direct dependency between the original java and scala examples
      (if pwd.last == "langchain4j-examples" then pwd else pwd / up) / RelPath(
        fileName,
      )
    ).toNIO
