package com.geeboff.cloudjammer.deserializer


import com.geeboff.cloudjammer.model.CustomField
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class DynamicFieldsDeserializer : JsonDeserializer<Map<String, List<CustomField>>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Map<String, List<CustomField>> {
        val result = mutableMapOf<String, List<CustomField>>()
        json.asJsonObject.entrySet().forEach { (key, value) ->
            val fields = value.asJsonObject["Fields"].asJsonArray
            val customFields = fields.map { context.deserialize<CustomField>(it, CustomField::class.java) }
            result[key] = customFields
        }
        return result
    }
}