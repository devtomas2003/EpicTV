package pt.spacelabs.experience.epictv

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.ParcelUuid
import android.provider.Settings
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.spacelabs.experience.epictv.adapters.NearbyAdapter
import java.util.UUID

class Bluetooth : ComponentActivity() {
    private val REQUEST_BLUETOOTH_PERMISSIONS = 1
    private lateinit var nearbyAdapter: NearbyAdapter
    private val blueList = mutableListOf<String>()
    private val tempDeviceList = mutableListOf<String>()
    private lateinit var txtNotFound: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.bluetooth)

        findViewById<ImageView>(R.id.arrowpageback).setOnClickListener {
            onBackPressed()
        }

        txtNotFound = findViewById(R.id.noBlue)

        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
        } else {
            startBluetoothOperations()
        }

        val listBlue = findViewById<RecyclerView>(R.id.blueList)
        listBlue.layoutManager = LinearLayoutManager(this)
        listBlue.isNestedScrollingEnabled = false
        nearbyAdapter = NearbyAdapter(blueList)
        listBlue.adapter = nearbyAdapter

        txtNotFound.visibility = if (blueList.isEmpty()) TextView.VISIBLE else TextView.GONE
    }

    private fun hasBluetoothPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestBluetoothPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT
            ),
            REQUEST_BLUETOOTH_PERMISSIONS
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startBluetoothOperations()
            } else {
                AlertDialog.Builder(this)
                    .setTitle("Permissões necessárias")
                    .setMessage("A EpicTV precisa de permissões de Bluetooth para funcionar corretamente.")
                    .setPositiveButton("OK") { dialog, _ ->
                        dialog.dismiss()
                        finish()
                    }
                    .create()
                    .show()
            }
        }
    }

    private fun startBluetoothOperations() {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH)
            return
        }

        if (!hasBluetoothPermissions()) {
            requestBluetoothPermissions()
            return
        }

        iniciarPublicidadeEBusca(bluetoothAdapter)
    }

    @SuppressLint("MissingPermission")
    private fun iniciarPublicidadeEBusca(bluetoothAdapter: BluetoothAdapter) {
        val scanner = bluetoothAdapter.bluetoothLeScanner
        val scanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val device = result.device
                val deviceInfo = "${device.name ?: "Desconhecido"} - ${device.address}"

                runOnUiThread {
                    if (!blueList.contains(deviceInfo)) {
                        blueList.add(deviceInfo)
                        nearbyAdapter.addDevice(deviceInfo)
                        txtNotFound.visibility = TextView.GONE
                    }
                }
            }

            override fun onScanFailed(errorCode: Int) {
                AlertDialog.Builder(this@Bluetooth)
                    .setTitle("Aviso")
                    .setMessage("Erro ao iniciar a procura por dispositivos Bluetooth: $errorCode")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val scanFilter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")))
            .build()

        scanner.startScan(listOf(scanFilter), scanSettings, scanCallback)

        val scanPeriod: Long = 5000
        window.decorView.postDelayed({
            runOnUiThread {
                nearbyAdapter.removeMissingDevices(tempDeviceList)
                tempDeviceList.clear()

                txtNotFound.visibility = if (blueList.isEmpty()) TextView.VISIBLE else TextView.GONE
            }
        }, scanPeriod)

        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false)
            .build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB")))
            .setIncludeDeviceName(true)
            .build()

        val callback = object : AdvertiseCallback() {
            override fun onStartFailure(errorCode: Int) {
                AlertDialog.Builder(this@Bluetooth)
                    .setTitle("Aviso")
                    .setMessage("Erro ao iniciar publicidade BLE: $errorCode")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            }
        }
        advertiser?.startAdvertising(settings, data, callback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                AlertDialog.Builder(this@Bluetooth)
                    .setTitle("Bluetooth Desativado")
                    .setMessage("O Bluetooth precisa estar ativado para esta funcionalidade.")
                    .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                    .create()
                    .show()
            } else {
                iniciarPublicidadeEBusca(bluetoothAdapter)
            }
        }
    }

    companion object {
        private const val REQUEST_ENABLE_BLUETOOTH = 2
    }
}