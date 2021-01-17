package com.triple5.quizdown.connector

class MultipleChoice constructor(val category : String, val question: String, val nAnswers: Int)
{
    var wrongAnswers : ArrayList<String> = ArrayList(nAnswers)
    var correctAnswer : String = ""
    init {

    }
}