package tech.healthpay.keyboard.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.healthpay.keyboard.R

/**
 * QRScannerActivity for scanning payment QR codes.
 */
class QRScannerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)
        
        // TODO: Implement QR scanning with CameraX or ML Kit
        Toast.makeText(this, "QR Scanner - Coming soon", Toast.LENGTH_SHORT).show()
    }
}
