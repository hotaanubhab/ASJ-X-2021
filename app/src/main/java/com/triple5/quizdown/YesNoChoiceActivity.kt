package com.triple5.quizdown

import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.triple5.quizdown.connector.Difficulty
import com.triple5.quizdown.connector.OpenTrivia
import com.triple5.quizdown.connector.YesNoQuestion
import kotlinx.android.synthetic.main.activity_yes_no_choice.*
import java.lang.ref.WeakReference

private const val TAG = "YesNoChoiceActivity"

class YesNoChoiceActivity : AppCompatActivity() {

    private var categoryId: Int = 0
    private var difficulty: Difficulty = Difficulty.easy
    private var score: Int = 0
    private var yesNoQuestion: YesNoQuestion = YesNoQuestion("","", false)
    private var token: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "YesNoActivitiy onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yes_no_choice)

        // load question asynch
        // category and difficulty are passed as intent params
        categoryId = intent.getIntExtra(EXTRA_MESSAGE_CAT_ID, 0)
        difficulty = Difficulty.valueOf(intent.getStringExtra(EXTRA_MESSAGE_DIFF))
        token = intent.getStringExtra(EXTRA_MESSAGE_CAT_TOKEN)

        // initialize info
        updateInfoText(this, difficulty, score)
        AsynchRetrieveYesNo(this, categoryId, difficulty).execute()
    }

    fun radioButtonClicked(view: View) {
        if (view is ImageButton) {
            if (view.id == R.id.buttonTrue && yesNoQuestion.isTrue ||
                view.id == R.id.buttonFalse && !yesNoQuestion.isTrue) {

                findViewById<ImageButton>(view.id).setBackgroundColor(Color.GREEN)
                Toast.makeText(this, "Correct!", Toast.LENGTH_LONG).show()
                score += when (difficulty) {
                    Difficulty.easy -> 5
                    Difficulty.medium -> 7
                    Difficulty.hard -> 10
                }

            }  else {
                findViewById<ImageButton>(view.id).setBackgroundColor(Color.RED)
                Toast.makeText(this, "Wrong", Toast.LENGTH_LONG).show()

            }

            // update info
            updateInfoText(this, difficulty, score)
            findViewById<Button>(R.id.nextButton).isEnabled = true
        }
    }

    private fun updateInfoText(activity : YesNoChoiceActivity, difficulty: Difficulty, score: Int) {
        findViewById<TextView>(R.id.game_info).text = "Difficulty: $difficulty -  Score: $score"
        findViewById<ImageButton>(R.id.buttonTrue).isEnabled = false
        findViewById<ImageButton>(R.id.buttonFalse).isEnabled = false
    }

    fun nextBtnClicked(view : View) {
        Log.d(TAG, "next button")
        AsynchRetrieveYesNo(this, categoryId, difficulty).execute()
    }

    companion object {
        class AsynchRetrieveYesNo constructor (activity : YesNoChoiceActivity, val categoryId : Int, val difficulty: Difficulty) : AsyncTask<Int, Int, Int>() {

            private val activityRef : WeakReference<YesNoChoiceActivity> = WeakReference(activity)

            override fun onPreExecute() {
                activity()?.let { it.progressBar.visibility = View.VISIBLE }
            }

            override fun doInBackground(vararg params: Int?): Int {
                Log.d(TAG, "AsynchRetrieveMultipleChoice.doInBackground ..")
                val activity = activity()?: return -1;

                //retrieve multiple choice
                try {
                    val yesNoQuestion = OpenTrivia().getTrueFalseQuestion(categoryId, difficulty, activity.token)
                    activity.yesNoQuestion = yesNoQuestion
                    // populate UI with question and answer
                    activity.runOnUiThread {
                        activity.findViewById<TextView>(R.id.categoryName).text = yesNoQuestion.category
                        activity.question.text = yesNoQuestion.question
                        activity.findViewById<Button>(R.id.nextButton).isEnabled = false

                        activity.findViewById<ImageButton>(R.id.buttonTrue).isEnabled = true
                        activity.findViewById<ImageButton>(R.id.buttonFalse).isEnabled = true
                        activity.findViewById<ImageButton>(R.id.buttonTrue).setBackgroundColor(Color.TRANSPARENT)
                        activity.findViewById<ImageButton>(R.id.buttonFalse).setBackgroundColor(Color.TRANSPARENT)

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

            private fun activity() : YesNoChoiceActivity? {
                val activity = activityRef.get()
                if (activity != null && !activity.isFinishing) {
                    return activity
                }
                return null
            }

        }
    }
}