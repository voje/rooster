package dev.voje.rooster

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TimePicker
import android.widget.Toast
import com.google.android.material.timepicker.TimeFormat
import java.text.DateFormat
import java.util.*

/*
    Hard-coded snooze guard.
    Users can't set next alarm inside the defined interval.
 */
const val SNOOZE_GUARD_MIN = 240
const val MINUTES_IN_DAY = 24 * 60

lateinit var mediaPlayer : MediaPlayer

class MainActivity : AppCompatActivity(), TimePickerDialog.OnTimeSetListener {


    private var currHourOfDay = 0
    private var currMinute = 0

    lateinit var alarmManager : AlarmManager

    private fun updateTime() {
        val cal = Calendar.getInstance()
        currHourOfDay = cal.get(Calendar.HOUR_OF_DAY)
        currMinute = cal.get(Calendar.MINUTE)
        Log.d("Rooster", "Current time is: $currHourOfDay, $currMinute")
    }

    private fun setAlarm(ctx : android.content.Context, intervalMillis: Long) {
        val intent = Intent(ctx, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
                ctx, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val absoluteMillis = System.currentTimeMillis() + intervalMillis
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, absoluteMillis, pendingIntent)

        val cal = Calendar.getInstance();
        cal.timeInMillis = absoluteMillis;
        val formatted = DateFormat.getTimeInstance(TimeFormat.CLOCK_24H).format(cal.time);
        val msg = "Alarm armed for $formatted"
        Log.d("Rooster", msg)
        Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        mediaPlayer = MediaPlayer.create(this, R.raw.igorrr_chicken_sonata)
        mediaPlayer.setLooping(true)

        findViewById<Button>(R.id.button_set_alarm).setOnClickListener {
            updateTime()
            Log.d("Rooster", "Set Alarm")
            TimePickerDialog(
                    this,
                    this,
                    currHourOfDay,
                    currMinute,
                    true
            ).show()
        }

        findViewById<Button>(R.id.button_shut_up).setOnClickListener {
            Log.d("Rooster", "Disable alarm")
            mediaPlayer.pause()
            mediaPlayer.seekTo(0)
            Toast.makeText(this, "Alarm disabled", Toast.LENGTH_LONG).show()
            /*
            TODO("We're just stopping the MediaPlayer; need a way to actually disable the alarm")
             */
        }
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        updateTime()
        val diffInMin = Math.floorMod(
            ((hourOfDay * 60 + minute) - (currHourOfDay * 60 + currMinute)), MINUTES_IN_DAY
        )
        Log.d("Rooster", "Picked time: $hourOfDay, $minute")
        Log.d("Rooster", "Difference in minutes: $diffInMin")
        if (diffInMin >= SNOOZE_GUARD_MIN) {
            Log.d("Rooster", "Setting alarm.")
            val diffInMillis = (diffInMin * 60 * 1000).toLong()
            setAlarm(this, diffInMillis)
        } else {
            val msg = "Can't set alarm; pick an interval larger than $SNOOZE_GUARD_MIN minutes"
            Log.d("Rooster", msg)
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("Rooster", "Ring Ring !!! (TODO)")
        mediaPlayer.start()
    }
}

