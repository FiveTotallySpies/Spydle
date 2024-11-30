package dev.totallyspies.spydle.shared;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import java.lang.reflect.Field;

public class JsonValidator {

  public static void validateJsonElement(JsonElement object, Class<?> clazz) {
    if (!object.isJsonObject()) {
      throw new JsonSyntaxException("Expected a JSON object");
    }

    JsonObject jsonObject = object.getAsJsonObject();

    // Iterate over fields in the GameServer class
    for (Field field : clazz.getDeclaredFields()) {
      String fieldName = field.getName();

      // Check if JSON has the field
      if (!jsonObject.has(fieldName)) {
        throw new JsonSyntaxException("Field '" + fieldName + "' is missing");
      }

      // Get the field's type in the GameServer class
      Class<?> fieldType = field.getType();
      JsonElement element = jsonObject.get(fieldName);

      // Validate the type in JSON matches the type in the GameServer class
      if ((fieldType == String.class && !element.isJsonPrimitive())
          || (fieldType == int.class
              && (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()))
          || (fieldType == boolean.class
              && (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()))
          || (fieldType.isEnum()
              && (!element.isJsonPrimitive() || !isValidEnum(fieldType, element.getAsString())))) {
        throw new JsonSyntaxException("Field '" + fieldName + "' is of incorrect type");
      }
    }
  }

  // Helper to validate if the JSON element matches the Enum type
  private static boolean isValidEnum(Class<?> enumType, String value) {
    for (Object constant : enumType.getEnumConstants()) {
      if (constant.toString().equals(value)) {
        return true;
      }
    }
    return false;
  }
}
