package pt.spacelabs.experience.epictv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.ScanOptions.QR_CODE

class QrCodeReader : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val options = ScanOptions()
        options.setDesiredBarcodeFormats(QR_CODE)
        options.setPrompt("Scan a GIFT Card")
        options.setCameraId(0)
        options.setBeepEnabled(false)
        options.setBarcodeImageEnabled(true)
        barcodeLauncher.launch(options)
    }

    private val barcodeLauncher = registerForActivityResult<ScanOptions, ScanIntentResult>(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            setResult(RESULT_CANCELED)
        } else {
            val data = Intent().apply {
                putExtra("SCANNED_RESULT", result.contents)
            }
            setResult(RESULT_OK, data)
        }
        finish()
    }
}