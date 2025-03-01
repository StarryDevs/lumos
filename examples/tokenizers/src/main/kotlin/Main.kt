import com.fasterxml.jackson.databind.type.TypeFactory
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.starry.lumos.core.tokenizer.TokenBuffer
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.javaType
import kotlin.reflect.jvm.javaGetter
import kotlin.reflect.jvm.javaSetter
import kotlin.time.measureTime

@OptIn(ExperimentalStdlibApi::class)
fun main()  {
    val jacksonObjectMapper = jacksonObjectMapper()
    val settings = CustomTokenizer.Settings()
    for (field in settings::class.memberProperties) {
        if (field !is KMutableProperty1<*, *>) continue
        val fieldName = field.name
        val defaultValue = field.javaGetter!!.invoke(settings)
        print("$fieldName[$defaultValue] = ")
        val value = readlnOrNull()
        if (value.isNullOrEmpty()) continue
        else try{
            field.javaSetter!!.invoke(settings, jacksonObjectMapper.readValue(value, TypeFactory.rawClass(field.returnType.javaType)))
        } catch (_: Throwable) {}
    }
    while (true) {
        try {
            print("> ")
            val line = (readlnOrNull() ?: break).takeUnless { it.trim().isEmpty() } ?: continue
            val result: TokenBuffer
            val time = measureTime {
                result = TokenBuffer(CustomTokenizer(line).applySettings(settings).tokenize().asIterable())
            }
            println("[$time] $result")
        } catch (throwable: Throwable) {
            throwable.printStackTrace(System.out)
        }
    }
}