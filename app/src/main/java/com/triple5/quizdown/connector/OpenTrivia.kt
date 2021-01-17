package com.triple5.quizdown.connector

import android.text.Html
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject

private const val TAG = "OpenTrivia"
class OpenTrivia {
    val OpenTriviaApiUrl = "https://opentdb.com/api.php"
    val OpenTriviaCategoriesUrl = "https://opentdb.com/api_category.php"
    var okHttpClient : OkHttpClient = OkHttpClient()

    fun getCategories():List<Category> {
        Log.d(TAG, "getCategories")
        val request : Request = Request.Builder().url(OpenTriviaCategoriesUrl).build()
        val response : Response = okHttpClient.newCall(request).execute()
        val responseStr: String?  = response.body()?.string()
        Log.d(TAG, "respBody: ->$responseStr<-")

        //{
        //	"trivia_categories": [{
        //			"id": 9,
        //			"name": "General Knowledge"
        //		}, {
        //			"id": 10,
        //			"name": "Entertainment: Books"
        //		}
        //	]
        //}

        val categoriesJSON = JSONObject(responseStr)
        val categoriesJSONarray : JSONArray = categoriesJSON.getJSONArray("trivia_categories")
        val categories : ArrayList<Category> = ArrayList<Category>()
        for (i in 0 until categoriesJSONarray.length()) {
            val categoryJSON = categoriesJSONarray.getJSONObject(i)
            val id = categoryJSON.getInt("id")
            val name = categoryJSON.getString("name")
            categories.add(Category(id, name))
            Log.d(TAG, "category $id $name")
        }

        return categories
    }

    fun getMutltipleChoiceQuestion(categoryId : Int, level : Difficulty, token : String?) : MultipleChoice {
        Log.d(TAG, "getMultipleChoiceQuestion for category: $categoryId using token $token")
        val  url = "$OpenTriviaApiUrl?amount=1&category=$categoryId&difficulty=$level&type=multiple&token=$token";

        // call OpenTrivia and parse retrieved question and answers
        // https://opentdb.com/api.php?amount=1&category=11&difficulty=easy&type=multiple
        //{
        //"response_code": 0,
        //	"results": [{
        //			"category": "Entertainment: Film",
        //			"type": "multiple",
        //			"difficulty": "easy",
        //			"question": "&quot;The first rule is: you don&#039;t talk about it&quot; is a reference to which movie?",
        //			"correct_answer": "Fight Club",
        //			"incorrect_answers": ["The Island", "Unthinkable", "American Pie"]
        //		}
        //	]
        //}

        Log.d(TAG, "execute request $url")
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        val responseStr: String?  = response.body()?.string()
        Log.d(TAG, "respBody: >$responseStr<")
        // for test to see the progressbar ;)
        Thread.sleep(800)

        val resultJson = JSONObject(responseStr)
        val respCode = resultJson.getInt("response_code")
        if (respCode != 0)
            throw  Exception("Unexpected response code: $respCode")

        val mcResult = resultJson.getJSONArray("results").getJSONObject(0)
        var multipleChoice = MultipleChoice(
            Html.fromHtml(mcResult.getString("category")).toString(),
            Html.fromHtml(mcResult.getString("question")).toString(),
            4)
        multipleChoice.correctAnswer = Html.fromHtml(mcResult.getString("correct_answer")).toString()

        val wrongAnswers = mcResult.getJSONArray("incorrect_answers")
        for (i in 0 until wrongAnswers.length()) {
            multipleChoice.wrongAnswers.add(Html.fromHtml(wrongAnswers.getString(i)).toString())
        }
        return multipleChoice
    }

    fun getTrueFalseQuestion(categoryId : Int, difficulty : Difficulty, token : String?) : YesNoQuestion {
        Log.d(TAG, "getTrueFalseQuestion for category $categoryId anf token $token")
        val  url = "$OpenTriviaApiUrl?amount=1&category=$categoryId&difficulty=$difficulty&type=boolean&token=$token";
        Log.d(TAG, "execute request $url")
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        val responseStr: String?  = response.body()?.string()
        Log.d(TAG, "respBody: >$responseStr<")
        // for test to see the progressbar ;)
        Thread.sleep(600)

        val resultsJson = JSONObject(responseStr)
        val respCode = resultsJson.getInt("response_code")
        if (respCode != 0)
            throw  Exception("Unexpected response code: $respCode")

        val firstResult = resultsJson.getJSONArray("results").getJSONObject(0)
        val yesNo = YesNoQuestion(
            Html.fromHtml(firstResult.getString("category")).toString(),
            Html.fromHtml(firstResult.getString("question")).toString(),
            firstResult.getString("correct_answer").toUpperCase().equals("TRUE")
        )
        Log.d(TAG, "YesNo: {$yesNo")
        return yesNo
    }

    fun getToken() : String {
        Log.d(TAG, "retrieve token")
        val url = "https://opentdb.com/api_token.php?command=request"
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        val responseStr = response.body()?.string()
        val respBodyJson = JSONObject(responseStr)

        val respCode =  respBodyJson.getInt("response_code")
        if (respCode != 0)
            throw  Exception("Unexpected response code: $respCode")
        val token = respBodyJson.getString("token")
        Log.d(TAG, "token = $token")
        return token
    }
}