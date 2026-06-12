package com.example.dompetkeluarga.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dompetkeluarga.HomeFragment_user
import com.example.dompetkeluarga.Login
import com.example.dompetkeluarga.R

class DailyReminderWorker (context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Check if permissions are needed for API level 33+ and request if not granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Permission not granted, log the issue or handle as needed
                return Result.failure()
            }
        }

        // Create and show notification
        createNotification()
        return Result.success()
    }

    private fun createNotification() {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "daily_reminder_channel",
                "Daily Reminder",
                NotificationManager.IMPORTANCE_HIGH // Ensure this is high to appear outside the app
            ).apply {
                description = "Channel for daily reminders"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Create intent to launch the Login activity when the notification is tapped
        val intent = Intent(applicationContext, Login::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        try {
            val notification = NotificationCompat.Builder(applicationContext, "daily_reminder_channel")
                .setSmallIcon(R.drawable.user) // Replace with an appropriate icon
                .setContentTitle("Pengingat Pengeluaran Harian")
                .setContentText("Jangan lupa untuk mengisi pengeluaran hari ini!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Ensure sound/vibration
                .build()

            // Use NotificationManagerCompat to show the notification
            NotificationManagerCompat.from(applicationContext).notify(1, notification)
        } catch (e: SecurityException) {
            // Handle SecurityException if permissions aren't granted
            e.printStackTrace()
        }
    }
}