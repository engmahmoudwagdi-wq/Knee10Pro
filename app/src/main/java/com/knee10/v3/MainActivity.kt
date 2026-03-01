
package com.knee10.v3

import android.app.*
import android.content.*
import android.media.RingtoneManager
import android.os.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var titleText: TextView
    private lateinit var timerText: TextView
    private lateinit var streakText: TextView
    private lateinit var painInput: EditText
    private lateinit var startButton: Button

    private var currentExercise = 0

    private val exercises = listOf(
        Pair("تسخين خفيف", 120000L),
        Pair("رفع الساق المستقيمة", 180000L),
        Pair("Wall Sit خفيف", 180000L),
        Pair("Hip Bridge", 180000L),
        Pair("Clamshell", 180000L)
    )

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleText = findViewById(R.id.titleText)
        timerText = findViewById(R.id.timerText)
        streakText = findViewById(R.id.streakText)
        painInput = findViewById(R.id.painInput)
        startButton = findViewById(R.id.startButton)

        startButton.setOnClickListener {
            currentExercise = 0
            startExercise()
        }

        updateStreak()
        scheduleReminder()
    }

    private fun startExercise() {
        if (currentExercise >= exercises.size) {
            finishWorkout()
            return
        }

        val (name, duration) = exercises[currentExercise]
        titleText.text = name

        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                timerText.text = String.format("%02d:%02d", minutes, remainingSeconds)
            }

            override fun onFinish() {
                playSound()
                currentExercise++
                startExercise()
            }
        }.start()
    }

    private fun finishWorkout() {
        titleText.text = "✔ مبروك"
        timerText.text = "تم إنهاء تمرين 15 دقيقة"
        saveCompletion()
        updateStreak()
    }

    private fun playSound() {
        val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(applicationContext, notification)
        ringtone.play()
    }

    private fun saveCompletion() {
        val prefs = getSharedPreferences("Knee10Prefs", MODE_PRIVATE)
        val today = getTodayKey()

        val painLevel = painInput.text.toString()
        prefs.edit()
            .putString("lastDay", today)
            .putString("pain_$today", painLevel)
            .apply()
    }

    private fun updateStreak() {
        val prefs = getSharedPreferences("Knee10Prefs", MODE_PRIVATE)
        val lastDay = prefs.getString("lastDay", "")
        val today = getTodayKey()

        if (lastDay == today) {
            streakText.text = "✔ تمرين اليوم مكتمل"
        } else {
            streakText.text = "لم يتم التمرين اليوم"
        }
    }

    private fun getTodayKey(): String {
        val calendar = Calendar.getInstance()
        return "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.DAY_OF_YEAR)}"
    }

    private fun scheduleReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 45)
            set(Calendar.SECOND, 0)
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
