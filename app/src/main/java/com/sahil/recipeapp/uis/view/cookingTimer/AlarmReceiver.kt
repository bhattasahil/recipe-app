package com.sahil.recipeapp.uis.view.cookingTimer

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sahil.recipeapp.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        var timeWhenAlarmIsToBeSet = 1L

        if (p1?.extras?.containsKey("timeWhenAlarmIsToBeSet") == true) {
            timeWhenAlarmIsToBeSet = p1.extras?.getLong("timeWhenAlarmIsToBeSet") ?: 1
            Log.e("Alarm to be set ", " $timeWhenAlarmIsToBeSet")
        }
        if (p1?.extras?.containsKey("recipeName") == true) {
            val mediaPlayer: MediaPlayer? = MediaPlayer.create(p0, R.raw.sound_alarm)

            val isToStartAlarm = p1.action.equals("startAlarm")
            val notificationId = p1.extras?.getInt("notificationId") ?: 1
            Log.e("Alarm start garnai parne ho", " $isToStartAlarm")
            if (isToStartAlarm) {
                if (mediaPlayer != null && !mediaPlayer.isPlaying) {
                    mediaPlayer.start()
                }
                val recipeName = p1.extras?.getString("recipeName") ?: ""
                if (recipeName.isNotEmpty()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val importance = NotificationManager.IMPORTANCE_DEFAULT
                        val channel = NotificationChannel(
                            "CulinaryDelight",
                            "CulinaryDelightChannelName",
                            importance
                        ).apply {
                            description = "Culinary Delight Notification"
                        }
                        // Register the channel with the system.
                        val notificationManager: NotificationManager =
                            p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    val builder = NotificationCompat.Builder(p0!!, "CulinaryDelight")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Your meal is ready")
                        .setContentText("$recipeName completion alert")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)

                    with(NotificationManagerCompat.from(p0)) {
                        if (ActivityCompat.checkSelfPermission(
                                p0,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return
                        }
                        notify(notificationId, builder.build())
                    }
                }
            } else {
                Log.e("Cancel", "Huna paryo")
                val notificationManager: NotificationManager =
                    p0?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.cancel(notificationId)

                if (mediaPlayer != null && !mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }

                // TODO: Verify why cancel is not working
            }
        }


    }
}