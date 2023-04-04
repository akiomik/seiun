package io.github.akiomik.seiun.api.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.rawType
import io.github.akiomik.seiun.model.type.HasNsid
import io.github.akiomik.seiun.model.type.Union4
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import kotlin.reflect.full.companionObjectInstance

class Union4JsonAdapter<A : HasNsid, B : HasNsid, C : HasNsid, D : HasNsid> private constructor(
    private val elem1Adapter: JsonAdapter<A>,
    private val elem2Adapter: JsonAdapter<B>,
    private val elem3Adapter: JsonAdapter<C>,
    private val elem4Adapter: JsonAdapter<D>,
    private val elem1Id: String,
    private val elem2Id: String,
    private val elem3Id: String,
    private val elem4Id: String
) : JsonAdapter<Union4<A, B, C, D>>() {
    private val typeKey = "\$type"
    private val typeKeyOptions = JsonReader.Options.of(typeKey)
    private val elem1KeyOptions = JsonReader.Options.of(elem1Id)
    private val elem2KeyOptions = JsonReader.Options.of(elem2Id)
    private val elem3KeyOptions = JsonReader.Options.of(elem3Id)
    private val elem4KeyOptions = JsonReader.Options.of(elem4Id)

    override fun toJson(writer: JsonWriter, value: Union4<A, B, C, D>?) {
        writer.beginObject()
        val token = writer.beginFlatten()
        writer.name(typeKey)
        when (value) {
            is Union4.Element1 -> {
                writer.value(elem1Id)
                elem1Adapter.toJson(writer, value.value)
            }
            is Union4.Element2 -> {
                writer.value(elem2Id)
                elem2Adapter.toJson(writer, value.value)
            }
            is Union4.Element3 -> {
                writer.value(elem3Id)
                elem3Adapter.toJson(writer, value.value)
            }
            is Union4.Element4 -> {
                writer.value(elem4Id)
                elem4Adapter.toJson(writer, value.value)
            }
            else -> {} // null case
        }
        writer.endFlatten(token)
        writer.endObject()
    }

    override fun fromJson(reader: JsonReader): Union4<A, B, C, D> {
        val peek = reader.peekJson()
        return when (peek.use { determineEither(it) }) {
            is Union4.Element1 -> Union4.Element1(elem1Adapter.fromJson(reader)!!)
            is Union4.Element2 -> Union4.Element2(elem2Adapter.fromJson(reader)!!)
            is Union4.Element3 -> Union4.Element3(elem3Adapter.fromJson(reader)!!)
            is Union4.Element4 -> Union4.Element4(elem4Adapter.fromJson(reader)!!)
        }
    }

    private fun determineEither(reader: JsonReader): Union4<Unit, Unit, Unit, Unit> {
        reader.beginObject()

        while (reader.hasNext()) {
            if (reader.selectName(typeKeyOptions) == -1) {
                reader.skipName()
                reader.skipValue()
                continue
            }

            val elem1KeyIndex = reader.selectString(elem1KeyOptions)
            if (elem1KeyIndex >= 0) {
                return Union4.Element1(Unit)
            }

            val elem2KeyIndex = reader.selectString(elem2KeyOptions)
            if (elem2KeyIndex >= 0) {
                return Union4.Element2(Unit)
            }

            val elem3KeyIndex = reader.selectString(elem3KeyOptions)
            if (elem3KeyIndex >= 0) {
                return Union4.Element3(Unit)
            }

            val elem4KeyIndex = reader.selectString(elem4KeyOptions)
            if (elem4KeyIndex >= 0) {
                return Union4.Element4(Unit)
            }

            throw JsonDataException("Expected '$elem1Id' or '$elem2Id' or '$elem3Id' or '$elem4Id' but found '${reader.nextString()}'.")
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
            if (type.rawType != Union4::class.java) return null

            val (elem1Adapter, elem1Id) = retrieveAdapterAndId(type.actualTypeArguments[0], moshi)
            val (elem2Adapter, elem2Id) = retrieveAdapterAndId(type.actualTypeArguments[1], moshi)
            val (elem3Adapter, elem3Id) = retrieveAdapterAndId(type.actualTypeArguments[2], moshi)
            val (elem4Adapter, elem4Id) = retrieveAdapterAndId(type.actualTypeArguments[3], moshi)

            return Union4JsonAdapter(
                elem1Adapter,
                elem2Adapter,
                elem3Adapter,
                elem4Adapter,
                elem1Id,
                elem2Id,
                elem3Id,
                elem4Id
            ).nullSafe()
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
