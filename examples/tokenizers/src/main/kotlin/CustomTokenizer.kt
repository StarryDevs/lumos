import org.starry.lumos.core.Lumos
import org.starry.lumos.core.tokenizer.LexResult
import org.starry.lumos.core.tokenizer.SimpleTokenizer
import java.net.URI
import java.nio.CharBuffer

class CustomTokenizer(buffer: CharBuffer, uri: URI = Lumos.BLANK_URI, punctuations: String = DEFAULT_PUNCTUATIONS) : SimpleTokenizer(buffer, uri, punctuations) {
    constructor(content: CharSequence, uri: URI = Lumos.BLANK_URI, punctuations: String = DEFAULT_PUNCTUATIONS) : this(CharBuffer.wrap(content), uri, punctuations)

    data class Settings(var disableExponential: Boolean = true, var disableWhitespace: Boolean = true, var disableSpecialNumbers: Boolean = true)

    fun applySettings(customSettings: Settings) = apply {
        disableExponential = customSettings.disableExponential
        disableWhitespace = customSettings.disableWhitespace
        disableSpecialNumbers = customSettings.disableSpecialNumbers
    }

    override fun lexOnceBegin(): LexResult {
        return if (buffer.get() == '~') LexResult.Success(CustomToken)
        else LexResult.Pass
    }

}