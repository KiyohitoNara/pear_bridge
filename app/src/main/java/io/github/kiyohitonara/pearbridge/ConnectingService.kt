/*
 * Copyright 2023 Kiyohito Nara
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.kiyohitonara.pearbridge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import timber.log.Timber

class ConnectingService : Service() {
    private val binder: IBinder = LocalBinder()

    private var bluetoothPeripheral: BluetoothPeripheral? = null
    private var bluetoothPeripheralSelectedListener: BluetoothPeripheralSelectedListener? = null

    override fun onCreate() {
        super.onCreate()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.app_name), NotificationManager.IMPORTANCE_NONE))

        Timber.d("Service created.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("Service started.")

        bluetoothPeripheral = intent?.getSerializableExtra(EXTRA_PERIPHERAL) as BluetoothPeripheral?
        bluetoothPeripheral ?: run {
            Timber.e("Peripheral is null.")

            bluetoothPeripheralSelectedListener?.onBluetoothPeripheralUnselected()
            stopForeground(STOP_FOREGROUND_REMOVE)

            return START_NOT_STICKY
        }

        val pendingIntent: PendingIntent = Intent(this, MainActivity::class.java).let { notificationIntent ->
            PendingIntent.getActivity(this, REQUEST_CODE, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_phone_iphone)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.connecting_to_peripheral, bluetoothPeripheral!!.name))
            .setContentIntent(pendingIntent)
            .build()

        bluetoothPeripheralSelectedListener?.onBluetoothPeripheralSelected(bluetoothPeripheral!!)
        startForeground(NOTIFICATION_ID, notification)

        return START_REDELIVER_INTENT
    }

    override fun onBind(p0: Intent?): IBinder {
        Timber.d("Service bound.")

        return binder
    }

    fun setBluetoothPeripheralSelectedListener(listener: BluetoothPeripheralSelectedListener?) {
        Timber.d("BluetoothPeripheralSelectedListener set.")

        bluetoothPeripheralSelectedListener = listener
        bluetoothPeripheral?.let {
            bluetoothPeripheralSelectedListener?.onBluetoothPeripheralSelected(it)
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Timber.d("Service unbound.")

        bluetoothPeripheralSelectedListener = null

        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        Timber.d("Service destroyed.")
    }

    inner class LocalBinder : Binder() {
        fun getService(): ConnectingService {
            return this@ConnectingService
        }
    }

    companion object {
        const val EXTRA_PERIPHERAL = "io.github.kiyohitonara.pearbridge.extra.PERIPHERAL"

        private const val REQUEST_CODE = 0

        private const val NOTIFICATION_ID = 1

        private const val NOTIFICATION_CHANNEL_ID = "peripheral_connection_channel_01"
    }
}