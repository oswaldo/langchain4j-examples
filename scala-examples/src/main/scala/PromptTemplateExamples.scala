import dev.langchain4j.model.input.Prompt
import dev.langchain4j.model.input.PromptTemplate
import scala.jdk.CollectionConverters._

object PromptTemplateExamples:

  @main def oneVariablePromptTemplate() =
    val promptTemplate = PromptTemplate.from("Say 'hi' in {{it}}.")
    val prompt         = promptTemplate.apply("German")
    println(prompt.text())

  @main def multipleVariablePromptTemplate() =
    val promptTemplate = PromptTemplate.from("Say '{{text}}' in {{language}}.")
    val variables      = Map("text" -> "hi", "language" -> "German")
    val prompt         = promptTemplate.apply(variables.asJava)
    println(prompt.text())
