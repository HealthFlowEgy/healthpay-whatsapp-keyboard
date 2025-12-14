package tech.healthpay.keyboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.DefaultDecoderFactory

/**
 * QR Scanner Activity
 * 
 * Scans QR codes for payments and payment requests.
 * 
 * NO HILT - Uses Application singleton for dependencies
 */
class QRScannerActivity : AppCompatActivity() {

    private var barcodeView: DecoratedBarcodeView? = null
    private var hasScanned = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        setupViews()
        checkCameraPermission()
    }

    private fun setupViews() {
        barcodeView = findViewById(R.id.barcode_view)

        // Configure scanner for QR codes only
        barcodeView?.barcodeView?.decoderFactory = DefaultDecoderFactory(listOf(BarcodeFormat.QR_CODE))

        findViewById<ImageButton>(R.id.btn_close)?.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        findViewById<Button>(R.id.btn_grant_permission)?.setOnClickListener {
            requestCameraPermission()
        }
    }

    private fun checkCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                    == PackageManager.PERMISSION_GRANTED -> {
                startScanning()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                showPermissionRationale()
            }
            else -> {
                requestCameraPermission()
            }
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning()
            } else {
                showPermissionDenied()
            }
        }
    }

    private fun startScanning() {
        barcodeView?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tv_instructions)?.visibility = View.VISIBLE
        findViewById<TextView>(R.id.tv_permission_error)?.visibility = View.GONE
        findViewById<Button>(R.id.btn_grant_permission)?.visibility = View.GONE

        barcodeView?.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                if (hasScanned) return
                result?.let {
                    hasScanned = true
                    handleScanResult(it.text)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        })
    }

    private fun showPermissionRationale() {
        barcodeView?.visibility = View.GONE
        findViewById<TextView>(R.id.tv_instructions)?.visibility = View.GONE
        findViewById<TextView>(R.id.tv_permission_error)?.apply {
            visibility = View.VISIBLE
            text = getString(R.string.camera_permission_rationale)
        }
        findViewById<Button>(R.id.btn_grant_permission)?.visibility = View.VISIBLE
    }

    private fun showPermissionDenied() {
        barcodeView?.visibility = View.GONE
        findViewById<TextView>(R.id.tv_instructions)?.visibility = View.GONE
        findViewById<TextView>(R.id.tv_permission_error)?.apply {
            visibility = View.VISIBLE
            text = getString(R.string.camera_permission_denied)
        }
        findViewById<Button>(R.id.btn_grant_permission)?.visibility = View.VISIBLE
    }

    private fun handleScanResult(content: String) {
        barcodeView?.pause()

        when {
            isValidHealthPayQR(content) -> {
                setResult(Activity.RESULT_OK, Intent().apply {
                    putExtra(EXTRA_QR_CONTENT, content)
                    putExtra(EXTRA_QR_TYPE, parseQRType(content))
                })
                finish()
            }
            else -> {
                Toast.makeText(this, R.string.invalid_qr_code, Toast.LENGTH_SHORT).show()
                hasScanned = false
                barcodeView?.resume()
            }
        }
    }

    private fun isValidHealthPayQR(content: String): Boolean {
        return content.startsWith("healthpay://") ||
                content.startsWith("https://portal.healthpay.tech/") ||
                content.startsWith("https://portal.beta.healthpay.tech/") ||
                content.matches(Regex("^HP[A-Z0-9]{12,}$"))
    }

    private fun parseQRType(content: String): String {
        return when {
            content.contains("/pay") || content.contains("pay?") -> QR_TYPE_PAYMENT
            content.contains("/request") || content.contains("request?") -> QR_TYPE_REQUEST
            content.contains("/wallet") -> QR_TYPE_WALLET
            else -> QR_TYPE_UNKNOWN
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            barcodeView?.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        barcodeView?.pause()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001

        const val EXTRA_QR_CONTENT = "qr_content"
        const val EXTRA_QR_TYPE = "qr_type"

        const val QR_TYPE_PAYMENT = "payment"
        const val QR_TYPE_REQUEST = "request"
        const val QR_TYPE_WALLET = "wallet"
        const val QR_TYPE_UNKNOWN = "unknown"
    }
}
