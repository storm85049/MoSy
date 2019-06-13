package com.matthias.mosy.bluetooth

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import com.matthias.mosy.MainActivity
import com.matthias.mosy.Prefs
import java.util.*

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */

const val STATE_DISCONNECTED = 0
const val STATE_CONNECTING = 1
const val STATE_CONNECTED = 2

const val ACTION_GATT_CONNECTED = "com.example.bluetooth.le.ACTION_GATT_CONNECTED"
const val ACTION_GATT_DISCONNECTED = "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED"
const val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
const val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
const val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

class BluetoothLeService(bluetoothManager: BluetoothManager) : Service() {

    private var mBluetoothManager = bluetoothManager
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED
    private val serviceUUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val characteristicUUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    private lateinit var prefs:Prefs

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                prefs.setBluetoothEnabled(true)
                intentAction = ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                // Attempts to discover services after successful connection.
                mBluetoothGatt!!.discoverServices()
                broadcastUpdate(intentAction)

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                prefs.setBluetoothEnabled(false)
                intentAction = ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                broadcastUpdate(intentAction)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED)
            } else {
            }
        }

        override fun onCharacteristicRead(
                gatt: BluetoothGatt,
                characteristic: BluetoothGattCharacteristic,
                status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic)
            }
        }
    }

    private val mBinder = LocalBinder()

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String, characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
    }

    inner class LocalBinder : Binder() {
        internal val service: BluetoothLeService
            get() = this@BluetoothLeService
    }

    override fun onBind(intent: Intent): IBinder? {
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
        return super.onUnbind(intent)
    }

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        mBluetoothAdapter = mBluetoothManager.adapter
        prefs = MainActivity.prefs!!
        if (mBluetoothAdapter == null) {
            return false
        }
        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean? {
        if (mBluetoothAdapter == null || address == null) {
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
                && mBluetoothGatt != null
        ) {
            if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                return false
            }
        }

        val device = mBluetoothAdapter!!.getRemoteDevice(address)
        if (device == null) {
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        val status = mBluetoothGatt?.discoverServices()
        return status
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    private fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    private fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        disconnect()
        mBluetoothGatt!!.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(
            characteristic: BluetoothGattCharacteristic,
            enabled: Boolean
    ) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt!!.setCharacteristicNotification(characteristic, enabled)
    }

    fun write(input: String){
        if (mBluetoothGatt == null) {
            return
        }
        val characteristic = mBluetoothGatt!!.getService(serviceUUID).getCharacteristic(characteristicUUID)
        characteristic.value = input.toByteArray(Charsets.UTF_8)
        //mBluetoothGatt!!.setCharacteristicNotification(characteristic, true)
        mBluetoothGatt!!.writeCharacteristic(characteristic)
    }
}