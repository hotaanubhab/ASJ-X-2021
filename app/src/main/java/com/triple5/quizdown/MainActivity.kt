package com.triple5.quizdown

import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.triple5.quizdown.connector.OpenTrivia
import java.lang.ref.WeakReference

private const val TAG = "MainActivity";

const val EXTRA_MESSAGE_CAT_ID = "com.triple5.quizdown.msg.catId";
const val EXTRA_MESSAGE_CAT_NAME = "com.triple5.quizdown.msg.catName";
const val EXTRA_MESSAGE_DIFF = "com.triple5.quizdown.msg.diff";
const val EXTRA_MESSAGE_CAT_TOKEN = "com.triple5.quizdown.msg.token";

/**
 * main activity
 */
class MainActivity : AppCompatActivity() {

    var categoriesToIdMap = HashMap<String, Int>()
    var token : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        // load categories and populate with async Task
        //  and token
        val task = AsynchLoadCategories(this)
        task.execute("urlparam")

        // populate difficulty spinner
        val spinnerDiff : Spinner = findViewById(R.id.spinnerDifficulty)
        ArrayAdapter.createFromResource(
            this,
            R.array.difficulty_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinnerDiff.adapter = adapter
        }

    }

    fun startGame(view : View) {
        Log.d(TAG, "startGame - clicked")
        val spinner : Spinner = findViewById(R.id.spinnerCategory)
        val selectedItem = spinner.selectedItem
        Log.d(TAG, "selected item $selectedItem")
        // ToDo find possibility to add Id and Label to spinner
        val selectedId = categoriesToIdMap.get(selectedItem)?.toInt()
        Log.d(TAG, "selected Id = $selectedId")

        val spinnerDiff : Spinner = findViewById(R.id.spinnerDifficulty)
        val selectedDiff = spinnerDiff.selectedItem.toString()
        // pass the selected category and difficulty as extra params with the intent
        // also pass the selected difficulty
        val clazz = if (findViewById<RadioButton>(R.id.radioMultipleChoice).isChecked) {
            MultipleChoiceActivity::class.java
        } else {
            YesNoChoiceActivity::class.java
        }
        val intent = Intent(this, clazz).apply {
            putExtra(EXTRA_MESSAGE_CAT_ID, selectedId)
            putExtra(EXTRA_MESSAGE_DIFF, selectedDiff)
            putExtra(EXTRA_MESSAGE_CAT_NAME, selectedItem.toString())
            putExtra(EXTRA_MESSAGE_CAT_TOKEN, token)
        }
        startActivity(intent)
    }

    companion object {

        class AsynchLoadCategories internal constructor(context : MainActivity) : AsyncTask<String, String, String?>() {

            private val activityRef : WeakReference<MainActivity> = WeakReference(context)

            override fun onPreExecute() {
                val activity = activityRef.get()
                if (activity == null || activity.isFinishing) return
                activity.progressBar.visibility = View.VISIBLE
            }

            override fun doInBackground(vararg params: String?): String? {

                Log.d(TAG, "doRunInBackground param0: ${params[0]}")
                val activity = activityRef.get()
                if (activity == null || activity.isFinishing)
                    return null
                // fetch categories
                val categoriesArray : ArrayList<String> = ArrayList<String>();
                try {
                    activity.token = OpenTrivia().getToken()
                    Log.d(TAG,"retrieve categories from OpenTrivia");
                    for (cat in OpenTrivia().getCategories()) {
                        Log.d(TAG, "adding category ${cat.name} to list")
                        categoriesArray.add(cat.name)
                        activity.categoriesToIdMap.put(cat.name, cat.id)
                    }
                    // update spinner
                    // populate category spinner
                    // found in https://stackoverflow.com/questions/35449800/best-practice-to-implement-key-value-pair-in-android-spinner/35450251
                    // important only ui threads my change the UI
                    activity.runOnUiThread{
                        val categoriesSpinner: Spinner = activity.findViewById(R.id.spinnerCategory)
                        val categoriesAdapter = ArrayAdapter(activity, android.R.layout.simple_spinner_item, categoriesArray)
                        categoriesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        categoriesSpinner.adapter = categoriesAdapter
                    }
                } catch (e : Exception) {
                    Log.e(TAG, "Unexpected exc retrieving multiple choice. {$e.message}", e)
                    activity.runOnUiThread(Runnable {
                        Toast.makeText(activity, "An error occurred please retry later: ${e.message}", Toast.LENGTH_LONG).show()
                    })
                }
                return activity.token
            }

            override fun onPostExecute(result: String?) {

                val activity = activityRef.get()
                if (activity == null || activity.isFinishing) return
                // default select multiple choice
                activity.findViewById<RadioButton>(R.id.radioMultipleChoice).isChecked = true
                activity.progressBar.visibility = View.GONE
            }

        }
    }
}
