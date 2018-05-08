package com.example.lkllkllkl.imagesegmentation

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val map = HashMap<String, String>()
        map.put("person", "/static/xxx/xxx")
        map.put("bird", "/static/xxx/xxx")
        map.put("car", "/static/xxx/xxx")
        val json = "{'person': 'static/xxx/xxx','bird': 'static/xxx/xxx','car': 'static/xxx/xxx'}"
        val gson = Gson()
        val pathMap: Map<String, String> = gson.fromJson(json, object : TypeToken<Map<String, String>>() {}.type)
        println(pathMap)
    }
}
