private def getEnvVar(name: String): Option[String] =
  Option(System.getenv(name))

enum ApiKeys(val value: Option[String]):

  // You can use "demo" api key for demonstration purposes.
  // You can get your own OpenAI API key here: https://platform.openai.com/account/api-keys
  case OPENAI_API_KEY extends ApiKeys(getEnvVar("OPENAI_API_KEY").orElse(Some("demo")))

  // You can get your own HuggingFace API key here: https://huggingface.co/settings/tokens
  case HF_API_KEY extends ApiKeys(getEnvVar("HF_API_KEY"))

  // You can get your own Judge0 RapidAPI key here: https://rapidapi.com/judge0-official/api/judge0-ce
  case RAPID_API_KEY extends ApiKeys(getEnvVar("RAPID_API_KEY"))
