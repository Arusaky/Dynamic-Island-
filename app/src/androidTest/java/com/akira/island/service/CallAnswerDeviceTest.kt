package com.akira.island.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Person
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Process
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import com.akira.island.core.Controls
import com.akira.island.core.IslandBus
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CallAnswerDeviceTest {
    @SdkSuppress(minSdkVersion = 31)
    @Test fun incomingCallStyleUsesItsAnswerActionEvenWithAChronometer() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val action = context.packageName + ".TEST_ANSWER"
        val answered = CountDownLatch(1)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) { answered.countDown() }
        }
        ContextCompat.registerReceiver(context, receiver, IntentFilter(action), ContextCompat.RECEIVER_NOT_EXPORTED)
        val answer = PendingIntent.getBroadcast(context, 440, Intent(action).setPackage(context.packageName),
            PendingIntent.FLAG_IMMUTABLE)
        val decline = PendingIntent.getBroadcast(context, 441,
            Intent(action + ".DECLINE").setPackage(context.packageName), PendingIntent.FLAG_IMMUTABLE)
        val notification = Notification.Builder(context, "test")
            .setSmallIcon(android.R.drawable.sym_call_incoming)
            .setCategory(Notification.CATEGORY_CALL)
            .setWhen(System.currentTimeMillis())
            .setUsesChronometer(true)
            .setStyle(Notification.CallStyle.forIncomingCall(Person.Builder().setName("Caller").build(), decline, answer))
            .build()
        val pkg = "com.example.testvoip"
        val sbn = StatusBarNotification(pkg, pkg, 440, "answer", Process.myUid(), 0, 0,
            notification, Process.myUserHandle(), System.currentTimeMillis())
        val calls = IslandBus.calls.value
        try {
            instrumentation.runOnMainSync {
                NotificationParser.onPosted(context, sbn, initial = true)
                val parsed = checkNotNull(IslandBus.calls.value[sbn.key])
                assertTrue(parsed.incoming)
                assertEquals(answer, parsed.answer)
                Controls.answerCall(context, parsed)
            }
            assertTrue("The app answer action should run directly", answered.await(10, TimeUnit.SECONDS))
        } finally {
            instrumentation.runOnMainSync { IslandBus.calls.value = calls }
            context.unregisterReceiver(receiver)
            answer.cancel()
            decline.cancel()
        }
    }
}
