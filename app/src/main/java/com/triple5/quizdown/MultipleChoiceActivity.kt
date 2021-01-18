package com.triple5.quizdown

import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.triple5.quizdown.connector.Difficulty
import com.triple5.quizdown.connector.MultipleChoice
import com.triple5.quizdown.connector.OpenTrivia
import kotlinx.android.synthetic.main.activity_multiple_choice.*
import java.lang.ref.WeakReference

private const val TAG = "MultipleChoiceActivity"

class  MultipleChoiceActivity : AppCompatActivity() {


    private var iRightAnswer : Int = 0
    private var multipleChoice : MultipleChoice = MultipleChoice("","", 4)
    private var categoryId : Int = 0
    private var score : Int = 0
    private var difficulty : Difficulty = Difficulty.easy
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MultipleChoiceActivity.onCreate")
        // category and difficulty are passed as intent params
        categoryId = intent.getIntExtra(EXTRA_MESSAGE_CAT_ID, 0)
        difficulty = Difficulty.valueOf(intent.getStringExtra(EXTRA_MESSAGE_DIFF))
        val categoryName = intent.getStringExtra(EXTRA_MESSAGE_CAT_NAME)
        token = intent.getStringExtra(EXTRA_MESSAGE_CAT_TOKEN)
        Log.d(TAG, "categoryId: $categoryId difficulty: $difficulty")
        setContentView(R.layout.activity_multiple_choice)

        //clear and init fields
        // current category is retrieved from OpenTrivia response
        //findViewById<TextView>(R.id.categoryName).text = categoryName
        question.text = ""
        answer0.text = ""
        answer1.text = ""
        answer2.text = ""
        answer3.text = ""
        updateInfoText(this, difficulty, score)

        //load multiple choice for category and difficulty in asynch task
        val task = AsynchRetrieveMultipleChoice(this, categoryId, difficulty)
        task.execute(categoryId)
    }

    private fun updateInfoText(activity : MultipleChoiceActivity, difficulty: Difficulty, score: Int) {
        activity.findViewById<TextView>(R.id.game_info).text = "Difficulty: $difficulty -  Score: $score"
    }

    fun radioButtonClicked(view : View) {
        Log.d(TAG, "answer radio clicked")
        if (view is RadioButton) {
            val isChecked = view.isChecked
            var ok = false
            when (view.id) {
                R.id.answer0 ->  if (iRightAnswer == 0) ok = true
                R.id.answer1 ->  if (iRightAnswer == 1) ok = true
                R.id.answer2 ->  if (iRightAnswer == 2) ok = true
                R.id.answer3 ->  if (iRightAnswer == 3) ok = true
            }
            // color mark radio buttons
            markRadioAnswer(this.findViewById(R.id.answer0), 0)
            markRadioAnswer(this.findViewById(R.id.answer1), 1)
            markRadioAnswer(this.findViewById(R.id.answer2), 2)
            markRadioAnswer(this.findViewById(R.id.answer3), 3)

            if (ok) {
                Toast.makeText(this, "Correct :)", Toast.LENGTH_LONG).show()
                score += when (difficulty) {
                    Difficulty.easy -> 5
                    Difficulty.medium -> 7
                    Difficulty.hard -> 10
                }
                // update score
                updateInfoText(this, difficulty, score)

            } else {
                Toast.makeText(this, "Wrong :( -- Right answer is ´${multipleChoice.correctAnswer}´", Toast.LENGTH_LONG).show()
            }

            this.findViewById<Button>(R.id.nextButton).isEnabled = true

        }
    }

    fun nextBtnClicked(view : View) {
        Log.d(TAG, "next Btn clicked")
        // load next question
        //load multiple choice for category and difficulty in asynch task
        Log.d(TAG, "load next question")
        this.findViewById<RadioGroup>(R.id.radioGroup).isSelected = false
        // color mark radio buttons
        clearRadioAnswer(this.findViewById(R.id.answer0))
        clearRadioAnswer(this.findViewById(R.id.answer1))
        clearRadioAnswer(this.findViewById(R.id.answer2))
        clearRadioAnswer(this.findViewById(R.id.answer3))
        val task = AsynchRetrieveMultipleChoice(this, categoryId, difficulty)
        task.execute(categoryId)
    }

    companion object {
        class AsynchRetrieveMultipleChoice
        internal constructor (activity : MultipleChoiceActivity, val categoryId : Int, val difficulty: Difficulty) : AsyncTask<Int, Int, Int>() {

            private val activityRef : WeakReference<MultipleChoiceActivity> = WeakReference(activity)

            override fun onPreExecute() {
                activity()?.let { it.progressBar.visibility = View.VISIBLE }
            }
            override fun doInBackground(vararg params: Int?): Int {
                Log.d(TAG, "AsynchRetrieveMultipleChoice.doInBackground ..")
                val activity = activity()?: return -1
                //retrieve multiple choice
                try {
                    val multipleChoice = OpenTrivia().getMutltipleChoiceQuestion(categoryId, difficulty, activity.token)
                    activity.multipleChoice = multipleChoice
                    // populate UI with question and answer
                    activity.runOnUiThread {
                        activity.findViewById<TextView>(R.id.categoryName).text = multipleChoice.category
                        activity.question.text = multipleChoice.question
                        val iRight = Math.floor(Math.random() * 4).toInt()
                        var ii = 0
                        activity.iRightAnswer = iRight

                        activity.answer0.text = if (iRight == 0) {
                            multipleChoice.correctAnswer
                        } else {
                            multipleChoice.wrongAnswers[ii++]
                        }
                        activity.answer1.text = if (iRight == 1) {
                            multipleChoice.correctAnswer
                        } else {
                            multipleChoice.wrongAnswers[ii++]
                        }
                        activity.answer2.text = if (iRight == 2) {
                            multipleChoice.correctAnswer
                        } else {
                            multipleChoice.wrongAnswers[ii++]
                        }
                        activity.answer3.text = if (iRight == 3) {
                            multipleChoice.correctAnswer
                        } else {
                            multipleChoice.wrongAnswers[ii++]
                        }
                        activity.findViewById<Button>(R.id.nextButton).isEnabled = false

                    }

                }
                catch (e: Exception) {  // ToDo catch only OpenTrivia Exc here
                    Log.e(TAG, "Unexpected exc retrieving multiple choice. {$e.message}", e)
                    activity.runOnUiThread(Runnable {
                        Toast.makeText(activity, "An error occurred please retry later: ${e.message}", Toast.LENGTH_LONG).show()
                    })
                    return -1
                }
                return 0
            }
            override fun onPostExecute(result: Int) {
                activity()?.let { it.progressBar.visibility = View.GONE }
            }

            private fun activity() : MultipleChoiceActivity? {
                val activity = activityRef.get()
                if (activity != null && !activity.isFinishing) {
                    return activity
                }
                return null
            }
        }
    }

    private fun markRadioAnswer(radio : RadioButton, i: Int) {
        if (iRightAnswer == i) {
            radio.setBackgroundColor(Color.GREEN)
        } else {
            radio.setBackgroundColor(Color.RED)
        }
        radio.isEnabled = false
    }

    private fun clearRadioAnswer(radio : RadioButton) {
        radio.setBackgroundColor(Color.WHITE)
        radio.isChecked = false
        radio.isEnabled = true
    }
}
