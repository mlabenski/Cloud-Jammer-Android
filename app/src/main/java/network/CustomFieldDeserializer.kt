package network

import com.geeboff.cloudjammer.model.CustomField
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class CustomFieldDeserializer : JsonDeserializer<CustomField> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): CustomField {
        val jsonObject = json.asJsonObject

        val name = jsonObject.get("name").asString
        val type = jsonObject.get("type").asString
        val options = jsonObject.getAsJsonArray("options")?.map { it.asString }

            return CustomField(name, type, options)
    }
}
