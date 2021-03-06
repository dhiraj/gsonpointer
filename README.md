# GsonPointer
[![](https://jitpack.io/v/dhiraj/gsonpointer.svg)](https://jitpack.io/#dhiraj/gsonpointer)

Adds [JSON Pointer (RFC 6901)](https://tools.ietf.org/html/rfc6901) support to 
[Google Gson](https://github.com/google/gson) library. Source code is written in Kotlin by literally importing 
and modifying original fork by [John Lombardo](https://github.com/johnnylambada/gson). 

## Usage
This library provides a simple lookup implementation, that tries to always provide a valid Kotlin object for quick and easy dereferencing and direct usage of JSON pointers to read values from Gson's JsonParser when you don't want to keep making specific Kotlin data classes for one off use cases or specific error handling. By using the provided _safeX_ extension properties on JsonElement, you can rest assured that you'll always get a good value back instead of an Exception at runtime.

### Parse JSON using Gson
Consider a JSON error response that you're receiving from your API like this:

```JSON
{
    "email": [
        "Enter a valid email address."
    ]
}
```

Instead of using `Gson().fromJson()` we use Gson's `JsonParser` class to get a `JsonElement` pointing to the root of the JSON tree, like this:
```kotlin
    import com.google.gson.JsonParser
    import com.traversient.gsonpointer.pointer

    //body is inside a `use` closure from OKHTTP response
    val element = JsonParser().parse(body!!.charStream())
    // element is JsonElement
```
 
### Access and read value using a JsonPointer
Once you have a JsonElement you can get a JsonPointer with it as root, using the convenient Kotlin extension function provided by this library.
```kotlin
    val pointer = element.pointer
    //pointer is JsonPointer    
```

Once you have a JsonPointer, you can directly access a JsonPointer anywhere in the whole JSON tree using pointer syntax:
```kotlin
    val message = pointer.at("/email/0").safeString
    //message is the first error String, "Enter a valid email address"
```

### Safe value retreival 
The `at()` function always returns a valid JsonElement, even when there is no match. You can use the standard `isJsonPrimitive` or other similar methods to determine what you got back. You can more easily use the provided convenience get properties to get either the actual value at the node or a default value that you can check and flow against.

* `JsonElement.safeNumber` returns any `Number` found at the pointer, or a `0`
* `JsonElement.safeString` returns any `String` found at the pointer, or a `""`
* `JsonElement.safeBoolean` returns any `Boolean` found at the pointer, or a `false`
* `JsonElement.safeJsonObject` returns any `JsonObject` found at the pointer, or a `JsonObject()`, which works out to an empty JSON object:  `{}`
* `JsonElement.safeJsonArray` returns any `JsonArray` found at the pointer, or a `JsonArray()`, which works out to an empty JSON Array, `[]`



## Installation
GsonPointer uses JitPack.io to distribute the library.
Add the Jitpack repository to your root project level build.gradle with:
```gradle
allprojects {
    repositories {
        jcenter()
        maven {
            url "https://jitpack.io"
        }
        google()
    }
}
```

and to your app gradle:

```gradle
dependencies {
    implementation 'com.github.dhiraj:gsonpointer:0.2'
}
```
