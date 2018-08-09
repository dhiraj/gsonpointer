package com.traversient.gsonpointer

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * The JsonPointer class implements much of RFC6901 Json Pointers for GSON Json documents. It can
 * also set values of elements to any valid JsonElement using pointer. For Example:
 * `
 * final String originalJson   = "{}";
 * final String expectedValue  = "baz";
 * final String reasonPointer  = "/foo/bar";
 * final JsonPointer pointer   = new JsonPointer(originalJson);
 *
 * assertEquals(null,pointer.dereference(reasonPointer));
 * pointer.set(reasonPointer,new JsonPrimitive(expectedValue));
 * assertEquals(expectedValue,pointer.dereference(reasonPointer).getAsString());
 * System.out.println(pointer.getElement().toString());
` *  produces the following `
 * {"foo":{"bar":"baz"}}
` *
 */
class JsonPointer internal constructor(val element: JsonElement) {

    @JvmOverloads
    fun at(pointer: String, generation: Int = 0): JsonElement {
        val list = getPath(pointer, false)
        return list[list.size - 1 - generation]
    }

    operator fun set(pointer: String, value: JsonElement) {
        val last = getLastElement(pointer)
        val `object` = last.element.asJsonObject
        `object`.remove(last.name)
        `object`.add(last.name, value)
    }

    private fun getPath(pointer: String, mutative: Boolean): List<JsonElement> {
        val tokens = pointer.split("/".toRegex()).toTypedArray()
        val ret = ArrayList<JsonElement>(tokens.size)
        var element: JsonElement? = null
        for (i in tokens.indices) {
            val token = unescape(tokens[i])
            val tokenNext = if (i == tokens.size - 1) null else unescape(tokens[i + 1])
            if (i == 0) {
                element = this.element
                ret.add(element)
            } else {
                if (element!!.isJsonArray && "-" == token) {
                    element = onNewArrayElementRequested(element.asJsonArray)
                } else {
                    val seq = getInt(token)
                    if (seq < 0) {
                        val `object` = element.asJsonObject
                        element = if (`object`.has(token))
                            `object`.get(token)
                        else if (mutative)
                            onTokenNotFound(`object`, token, tokenNext)
                        else
                            null
                    } else {
                        val array = element.asJsonArray
                        element = if (seq < array.size())
                            array.get(seq)
                        else if (mutative)
                            onSequenceNotFound(array, seq, tokenNext)
                        else
                            null
                    }
                }
                if (element == null) {
                    break
                }
                ret.add(element)
            }
        }
        return ret
    }

    private fun unescape(s: String): String {
        return s.replace("~1".toRegex(), "/").replace("~0".toRegex(), "~")
    }

    private fun getInt(s: String): Int {
        var ret = -1
        try {
            ret = Integer.parseInt(s)
        } catch (ignored: NumberFormatException) {
        }

        return ret
    }

    private fun onSequenceNotFound(element: JsonArray, seq: Int, tokenNext: String?): JsonElement? {
        var newElement: JsonElement? = null
        for (i in element.size()..seq) {
            newElement = buildHolderOf(tokenNext)
            element.add(newElement)
        }
        return newElement
    }

    private fun onTokenNotFound(element: JsonObject, token: String, tokenNext: String?): JsonElement {
        val newElement = buildHolderOf(tokenNext)
        element.add(token, newElement)
        return newElement
    }

    private fun onNewArrayElementRequested(element: JsonArray): JsonElement {
        val `object` = JsonObject()
        element.add(`object`)
        return `object`
    }

    private fun getLastElement(pointer: String): Element {
        val lastSlash = pointer.lastIndexOf('/')
        val parentPointer = pointer.substring(0, lastSlash)
        val objectName = pointer.substring(lastSlash + 1)
        val list = getPath(parentPointer, true)
        val element = list[list.size - 1]
        return Element(element, objectName)
    }

    private fun buildHolderOf(token: String?): JsonElement {
        return if (token == null) {
            JsonObject()
        } else if ("-" == token || getInt(token) >= 0) {
            JsonArray()
        } else {
            JsonObject()
        }
    }

    private class Element(internal val element: JsonElement, internal val name: String)
}

//Extension property on JsonElement to get a pointer
val JsonElement.pointer: JsonPointer
    get() {
        return JsonPointer(this)
    }

val JsonElement.safeNumber: Number
    get() {
        if (this.isJsonPrimitive && !this.isJsonNull && this.asJsonPrimitive.isNumber) {
            return this.asNumber
        }
        return 0
    }

val JsonElement.safeString: String
    get() {
        if (this.isJsonPrimitive && !this.isJsonNull && this.asJsonPrimitive.isString) {
            return this.asString
        }
        return ""
    }

val JsonElement.safeBoolean: Boolean
    get() {
        if (this.isJsonPrimitive && !this.isJsonNull && this.asJsonPrimitive.isBoolean) {
            return this.asBoolean
        }
        return false
    }

val JsonElement.safeJsonObject: JsonObject
    get() {
        if (!this.isJsonNull && this.isJsonObject) {
            return this.asJsonObject
        }
        return JsonObject()
    }

val JsonElement.safeJsonArray: JsonArray
    get() {
        if (!this.isJsonNull && this.isJsonArray) {
            return this.asJsonArray
        }
        return JsonArray()
    }