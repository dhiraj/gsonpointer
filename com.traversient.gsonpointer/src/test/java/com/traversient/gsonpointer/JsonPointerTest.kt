package com.traversient.gsonpointer

import com.google.gson.*
import org.junit.Test

import org.junit.Assert.*

import com.traversient.gsonpointer.JsonPointer

import org.junit.Assert.assertEquals


/*
{
	"library": {
		"name": "library of congress",
		"section": [{
			"name": "sci-fi",
			"title": [{
				"book": {
					"name": "Mote in Gods Eye",
					"author": ["Larry Niven", "Jerry Pournelle"]
				}
			}, {
				"book": {
					"name": "Ringworld",
					"author": ["Larry Niven"]
				}
			}]
		}]
	}
}

RFC 6901
{
      "foo": ["bar", "baz"],
      "": 0,
      "a/b": 1,
      "c%d": 2,
      "e^f": 3,
      "g|h": 4,
      "i\\j": 5,
      "k\"l": 6,
      " ": 7,
      "m~n": 8
}
 */
class JsonPointerTest {
    private val JSON = "{\n" +
            "\t\"library\": {\n" +
            "\t\t\"name\": \"library of congress\",\n" +
            "\t\t\"section\": [{\n" +
            "\t\t\t\"name\": \"sci-fi\",\n" +
            "\t\t\t\"title\": [{\n" +
            "\t\t\t\t\"book\": {\n" +
            "\t\t\t\t\t\"name\": \"Mote in Gods Eye\",\n" +
            "\t\t\t\t\t\"author\": [\"Larry Niven\", \"Jerry Pournelle\"]\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}, {\n" +
            "\t\t\t\t\"book\": {\n" +
            "\t\t\t\t\t\"name\": \"Ringworld\",\n" +
            "\t\t\t\t\t\"copies\": 2000,\n" +
            "\t\t\t\t\t\"author\": [\"Larry Niven\"]\n" +
            "\t\t\t\t}\n" +
            "\t\t\t}]\n" +
            "\t\t}]\n" +
            "\t}\n" +
            "}"
    private val JSON_6901 = "{\n" +
            "      \"foo\": [\"bar\", \"baz\"],\n" +
            "      \"\": 0,\n" +
            "      \"a/b\": 1,\n" +
            "      \"c%d\": 2,\n" +
            "      \"e^f\": 3,\n" +
            "      \"g|h\": 4,\n" +
            "      \"i\\\\j\": 5,\n" +
            "      \"k\\\"l\": 6,\n" +
            "      \" \": 7,\n" +
            "      \"m~n\": 8\n" +
            "   }"

    private val JSON_Multi = "{\n" +
            "  \"widget\": {\n" +
            "    \"debug\": \"on\",\n" +
            "    \"window\": {\n" +
            "      \"title\": \"Sample Konfabulator Widget\",\n" +
            "      \"name\": \"main_window\",\n" +
            "      \"width\": 500,\n" +
            "      \"height\": 500\n" +
            "    },\n" +
            "    \"image\": {\n" +
            "      \"src\": \"Images/Sun.png\",\n" +
            "      \"name\": \"sun1\",\n" +
            "      \"hOffset\": 250,\n" +
            "      \"vOffset\": 250,\n" +
            "      \"alignment\": \"center\"\n" +
            "    },\n" +
            "    \"text\": {\n" +
            "      \"data\": \"Click Here\",\n" +
            "      \"size\": 36,\n" +
            "      \"style\": \"bold\",\n" +
            "      \"name\": \"text1\",\n" +
            "      \"hOffset\": 250,\n" +
            "      \"vOffset\": 100,\n" +
            "      \"alignment\": \"center\",\n" +
            "      \"onMouseUp\": \"sun1.opacity = (sun1.opacity / 100) * 90;\"\n" +
            "    }\n" +
            "  }\n" +
            "}"

    @Test
    @Throws(Exception::class)
            /**
             * See https://tools.ietf.org/html/rfc6901
             */
    fun rfc6901Test() {
        val root = JsonParser().parse(JSON_6901)
        val json = root.pointer
        assertEquals(root, json.at(""))
        assertEquals(JsonParser().parse("[\"bar\", \"baz\"]"), json.at("/foo"))
        assertEquals("bar", json.at("/foo/0")!!.getAsString())
        assertEquals(0, json.at("/")!!.getAsInt())
        assertEquals(1, json.at("/a~1b")!!.getAsInt())
        assertEquals(2, json.at("/c%d")!!.getAsInt())
        assertEquals(3, json.at("/e^f")!!.getAsInt())
        assertEquals(4, json.at("/g|h")!!.getAsInt())
        assertEquals(5, json.at("/i\\j")!!.getAsInt())
        assertEquals(6, json.at("/k\"l")!!.getAsInt())
        assertEquals(7, json.at("/ ")!!.getAsInt())
        assertEquals(8, json.at("/m~0n")!!.getAsInt())
    }


    @Test
    @Throws(Exception::class)
    fun constructorTest() {
        val expected = JsonParser().parse("{}")
        val json = expected.pointer
        assertEquals(expected, json.element)
    }

    @Test
    @Throws(Exception::class)
    fun getRootTest() {
        val expected = JsonParser().parse(JSON)
        val json = expected.pointer
        val actual = json.at("")
        assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun getSimpleStringTest() {
        val json = JsonParser().parse(JSON).pointer
        val expected = "library of congress"
        val actual = json.at("/library/name")!!.getAsString()
        assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun getSimpleStringInArrayTest() {
        val json = JsonParser().parse(JSON).pointer
        val expected = "sci-fi"
        val actual = json.at("/library/section/0/name")!!.getAsString()
        assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun getDeepArrayTest() {
        val json = JsonParser().parse(JSON).pointer
        val expected = "Jerry Pournelle"
        val actual = json.at("/library/section/0/title/0/book/author/1")!!.getAsString()
        assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun mutableTest() {
        val json = JsonParser().parse(JSON).pointer
        val expected = "hello world"
        val pointer = "/this/is/a/new/thing"
        json.set(pointer, JsonPrimitive(expected))
        val actual = json.at(pointer)!!.getAsString()
        assertEquals(expected, actual)
    }

    @Test
    @Throws(Exception::class)
    fun setStringTestWithArrays() {
        val originalJson = "[]"
        val originalValue = "hello earth"
        val expectedValue = "hello world"
        val thingPointer = "/4/this/is/a/0/new/thing"
        val pointer = JsonParser().parse(originalJson).pointer

        assertEquals(JsonArray(), pointer.at(thingPointer))
        pointer.set(thingPointer, JsonPrimitive(originalValue))
        assertEquals(originalValue, pointer.at(thingPointer)!!.getAsString())
        pointer.set(thingPointer, JsonPrimitive(expectedValue))
        assertEquals(expectedValue, pointer.at(thingPointer)!!.getAsString())
    }

    @Test
    @Throws(Exception::class)
    fun setStringTest() {
        val originalJson = "{}"
        val originalValue = "hello earth"
        val expectedValue = "hello world"
        val thingPointer = "/this/is/a/0/new/thing"
        val pointer = JsonParser().parse(originalJson).pointer

        assertEquals(JsonObject(), pointer.at(thingPointer))
        pointer.set(thingPointer, JsonPrimitive(originalValue))
        assertEquals(originalValue, pointer.at(thingPointer)!!.getAsString())
        pointer.set(thingPointer, JsonPrimitive(expectedValue))
        assertEquals(expectedValue, pointer.at(thingPointer)!!.getAsString())
    }

    @Test
    @Throws(Exception::class)
    fun setBooleanTest() {
        val originalJson = "{}"
        val originalValue = false
        val expectedValue = true
        val thingPointer = "/this/is/a/0/new/thing"
        val pointer = JsonParser().parse(originalJson).pointer

        assertEquals(JsonObject(), pointer.at(thingPointer))
        pointer.set(thingPointer, JsonPrimitive(originalValue))
        assertEquals(originalValue, pointer.at(thingPointer)!!.getAsBoolean())
        pointer.set(thingPointer, JsonPrimitive(expectedValue))
        assertEquals(expectedValue, pointer.at(thingPointer)!!.getAsBoolean())
    }

    @Test
    @Throws(Exception::class)
    fun getThenSetTest() {
        val originalJson = "{}"
        val expectedValue = "a reason"
        val reasonPointer = "/data/extensions/currentVisit/reason"
        val pointer = JsonParser().parse(originalJson).pointer

        assertEquals(JsonObject(), pointer.at(reasonPointer))
        pointer.set(reasonPointer, JsonPrimitive(expectedValue))
        assertEquals(expectedValue, pointer.at(reasonPointer)!!.getAsString())
    }

    @Test
    @Throws(Exception::class)
    fun setTest() {
        val originalJson = "{}"
        val thingPointer = "/this/is/a/0/new/thing"
        val pointer = JsonParser().parse(originalJson).pointer
        val value1 = "ading"
        val value2 = 1
        val value3 = true

        assertEquals(JsonObject(), pointer.at(thingPointer))
        pointer.set(thingPointer, JsonPrimitive(value1))
        assertEquals(value1, pointer.at(thingPointer)!!.getAsString())
        pointer.set(thingPointer, JsonPrimitive(value2))
        assertEquals(value2, pointer.at(thingPointer)!!.getAsInt())
        pointer.set(thingPointer, JsonPrimitive(value3))
        assertEquals(value3, pointer.at(thingPointer)!!.getAsBoolean())
    }

    @Test
    @Throws(Exception::class)
    fun exampleTest() {
        val originalJson = "{}"
        val expectedValue = "baz"
        val reasonPointer = "/foo/bar"
        val pointer = JsonParser().parse(originalJson).pointer

        assertEquals(JsonObject(), pointer.at(reasonPointer))
        pointer.set(reasonPointer, JsonPrimitive(expectedValue))
        assertEquals(expectedValue, pointer.at(reasonPointer)!!.getAsString())
        println(pointer.element.toString())
    }
}
