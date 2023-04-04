package io.github.akiomik.seiun.api.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.rawType
import io.github.akiomik.seiun.model.type.HasNsid
import io.github.akiomik.seiun.model.type.Union2
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.full.companionObjectInstance

class Union2JsonAdapter<A : HasNsid, B : HasNsid> private constructor(
    private val elem1Adapter: JsonAdapter<A>,
    private val elem2Adapter: JsonAdapter<B>,
    private val elem1Id: String,
    private val elem2Id: String
) : JsonAdapter<Union2<A, B>>() {
    private val typeKey = "\$type"
    private val typeKeyOptions = JsonReader.Options.of(typeKey)
    private val elem1KeyOptions = JsonReader.Options.of(elem1Id)
    private val elem2KeyOptions = JsonReader.Options.of(elem2Id)

    override fun toJson(writer: JsonWriter, value: Union2<A, B>?) {
        writer.beginObject()
        val token = writer.beginFlatten()
        writer.name(typeKey)
        when (value) {
            is Union2.Element1 -> {
                writer.value(elem1Id)
                elem1Adapter.toJson(writer, value.value)
            }
            is Union2.Element2 -> {
                writer.value(elem2Id)
                elem2Adapter.toJson(writer, value.value)
            }
            else -> {} // null case
        }
        writer.endFlatten(token)
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): Union2<A, B> {
        val peek = reader.peekJson()
        return when (peek.use { determineEither(it) }) {
            is Union2.Element1 -> Union2.Element1(elem1Adapter.fromJson(reader)!!)
            is Union2.Element2 -> Union2.Element2(elem2Adapter.fromJson(reader)!!)
        }
    }

    private fun determineEither(reader: JsonReader): Union2<Unit, Unit> {
        reader.beginObject()

        while (reader.hasNext()) {
            if (reader.selectName(typeKeyOptions) == -1) {
                reader.skipName()
                reader.skipValue()
                continue
            }

            val elem1KeyIndex = reader.selectString(elem1KeyOptions)
            if (elem1KeyIndex >= 0) {
                return Union2.Element1(Unit)
            }

            val elem2KeyIndex = reader.selectString(elem2KeyOptions)
            if (elem2KeyIndex >= 0) {
                return Union2.Element2(Unit)
            }

            throw JsonDataException("Expected '$elem1Id' or '$elem2Id' but found '${reader.nextString()}'.")
        }

        reader.endObject()
        throw JsonDataException("$typeKey is missing.")
    }

    companion object Factory : JsonAdapter.Factory {
        override fun create(
            type: Type,
            annotations: Set<Annotation>,
            moshi: Moshi
        ): JsonAdapter<*>? {
            if (annotations.isNotEmpty()) return null

            if (type !is ParameterizedType) return null
            if (type.rawType != Union2::class.java) return null

            val (elem1Adapter, elem1Id) = retrieveAdapterAndId(type.actualTypeArguments[0], moshi)
            val (elem2Adapter, elem2Id) = retrieveAdapterAndId(type.actualTypeArguments[1], moshi)

            return Union2JsonAdapter(elem1Adapter, elem2Adapter, elem1Id, elem2Id).nullSafe()
        }

        private fun retrieveAdapterAndId(
            type: Type,
            moshi: Moshi
        ): Pair<JsonAdapter<HasNsid>, String> {
            val adapter = moshi.adapter<HasNsid>(type).nullSafe()
            val companion = type.rawType.kotlin.companionObjectInstance
            val id = (companion as HasNsid?)?.nsid
                ?: throw IllegalStateException("Can't resolve id for $type")

            return Pair(adapter, id)
        }
    }
}
