package tech.healthpay.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R

/**
 * LoginActivity handles the mobile number + OTP authentication flow.
 * 
 * Flow:
 * 1. User enters mobile number
 * 2. User taps "Send OTP"
 * 3. API sends OTP to the mobile number
 * 4. User enters the received OTP
 * 5. User taps "Verify & Login"
 * 6. API verifies OTP and returns access token
 * 7. On success, redirect to BiometricAuthActivity to setup biometric
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        const val RESULT_LOGIN_SUCCESS = 100
        const val RESULT_LOGIN_CANCELLED = 101
        private const val OTP_RESEND_DELAY_MS = 60000L // 60 seconds
        const val REQUEST_BIOMETRIC_SETUP = 1001
    }

    // UI Elements - Phone Number Step
    private lateinit var phoneNumberLayout: LinearLayout
    private lateinit var etPhoneNumber: EditText
    private lateinit var btnSendOtp: Button
    private lateinit var progressSendOtp: ProgressBar

    // UI Elements - OTP Verification Step
    private lateinit var otpVerificationLayout: LinearLayout
    private lateinit var tvPhoneDisplay: TextView
    private lateinit var btnChangeNumber: TextView
    private lateinit var etOtp1: EditText
    private lateinit var etOtp2: EditText
    private lateinit var etOtp3: EditText
    private lateinit var etOtp4: EditText
    private lateinit var etOtp5: EditText
    private lateinit var etOtp6: EditText
    private lateinit var btnVerifyOtp: Button
    private lateinit var progressVerifyOtp: ProgressBar
    private lateinit var tvResendOtp: TextView
    private lateinit var tvResendTimer: TextView

    // State
    private var currentPhoneNumber: String = ""
    private var otpRequestId: String? = null
    private var resendTimer: CountDownTimer? = null
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    // Dependencies
    private val apiClient by lazy { HealthPayKeyboardApplication.apiClient }
    private val tokenManager by lazy { HealthPayKeyboardApplication.tokenManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initViews()
        setupPhoneNumberStep()
        setupOtpVerificationStep()
        
        // Start with phone number step
        showPhoneNumberStep()
    }

    private fun initViews() {
        // Phone Number Step
        phoneNumberLayout = findViewById(R.id.layout_phone_number)
        etPhoneNumber = findViewById(R.id.et_phone_number)
        btnSendOtp = findViewById(R.id.btn_send_otp)
        progressSendOtp = findViewById(R.id.progress_send_otp)

        // OTP Verification Step
        otpVerificationLayout = findViewById(R.id.layout_otp_verification)
        tvPhoneDisplay = findViewById(R.id.tv_phone_display)
        btnChangeNumber = findViewById(R.id.btn_change_number)
        etOtp1 = findViewById(R.id.et_otp_1)
        etOtp2 = findViewById(R.id.et_otp_2)
        etOtp3 = findViewById(R.id.et_otp_3)
        etOtp4 = findViewById(R.id.et_otp_4)
        etOtp5 = findViewById(R.id.et_otp_5)
        etOtp6 = findViewById(R.id.et_otp_6)
        btnVerifyOtp = findViewById(R.id.btn_verify_otp)
        progressVerifyOtp = findViewById(R.id.progress_verify_otp)
        tvResendOtp = findViewById(R.id.tv_resend_otp)
        tvResendTimer = findViewById(R.id.tv_resend_timer)
    }

    private fun setupPhoneNumberStep() {
        // Phone number validation
        etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val phone = s?.toString()?.trim() ?: ""
                btnSendOtp.isEnabled = isValidPhoneNumber(phone)
            }
        })

        // Send OTP button
        btnSendOtp.setOnClickListener {
            val phone = etPhoneNumber.text.toString().trim()
            if (isValidPhoneNumber(phone)) {
                sendOtp(phone)
            }
        }
    }

    private fun setupOtpVerificationStep() {
        // Setup OTP input auto-advance
        val otpFields = listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)
        
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.length == 1 && index < otpFields.size - 1) {
                        // Move to next field
                        otpFields[index + 1].requestFocus()
                    }
                    // Enable verify button when all fields are filled
                    btnVerifyOtp.isEnabled = getOtpCode().length == 6
                }
            })

            // Handle backspace to go to previous field
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == android.view.KeyEvent.KEYCODE_DEL && 
                    event.action == android.view.KeyEvent.ACTION_DOWN &&
                    editText.text.isEmpty() && index > 0) {
                    otpFields[index - 1].apply {
                        requestFocus()
                        setText("")
                    }
                    true
                } else {
                    false
                }
            }
        }

        // Change number button
        btnChangeNumber.setOnClickListener {
            showPhoneNumberStep()
        }

        // Verify OTP button
        btnVerifyOtp.setOnClickListener {
            verifyOtp()
        }

        // Resend OTP
        tvResendOtp.setOnClickListener {
            if (tvResendOtp.isEnabled) {
                sendOtp(currentPhoneNumber)
            }
        }
    }

    private fun showPhoneNumberStep() {
        phoneNumberLayout.visibility = View.VISIBLE
        otpVerificationLayout.visibility = View.GONE
        resendTimer?.cancel()
    }

    private fun showOtpVerificationStep() {
        phoneNumberLayout.visibility = View.GONE
        otpVerificationLayout.visibility = View.VISIBLE
        
        // Display masked phone number
        tvPhoneDisplay.text = maskPhoneNumber(currentPhoneNumber)
        
        // Clear OTP fields
        listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6).forEach { 
            it.setText("") 
        }
        etOtp1.requestFocus()
        
        // Start resend timer
        startResendTimer()
    }

    private fun isValidPhoneNumber(phone: String): Boolean {
        // Egyptian phone number validation: starts with 01 and has 11 digits
        // Or international format starting with +20
        val egyptianPattern = "^01[0125][0-9]{8}$".toRegex()
        val internationalPattern = "^\\+20[0-9]{10}$".toRegex()
        return phone.matches(egyptianPattern) || phone.matches(internationalPattern)
    }

    private fun maskPhoneNumber(phone: String): String {
        if (phone.length < 6) return phone
        val visible = 4
        val start = phone.take(visible)
        val end = phone.takeLast(2)
        val masked = "*".repeat(phone.length - visible - 2)
        return "$start$masked$end"
    }

    private fun getOtpCode(): String {
        return listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6)
            .joinToString("") { it.text.toString() }
    }

    private fun startResendTimer() {
        tvResendOtp.isEnabled = false
        tvResendOtp.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        tvResendTimer.visibility = View.VISIBLE

        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(OTP_RESEND_DELAY_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                tvResendTimer.text = String.format("(%02d:%02d)", seconds / 60, seconds % 60)
            }

            override fun onFinish() {
                tvResendOtp.isEnabled = true
                tvResendOtp.setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.healthpay_primary))
                tvResendTimer.visibility = View.GONE
            }
        }.start()
    }

    // ==================== API CALLS ====================

    private fun sendOtp(phoneNumber: String) {
        currentPhoneNumber = phoneNumber
        
        // Show loading
        btnSendOtp.isEnabled = false
        progressSendOtp.visibility = View.VISIBLE

        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.requestOtp(phoneNumber)
                }

                result.fold(
                    onSuccess = { response ->
                        otpRequestId = response.requestId
                        Log.d(TAG, "OTP sent successfully. RequestId: ${response.requestId}")
                        showOtpVerificationStep()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to send OTP", error)
                        Toast.makeText(
                            this@LoginActivity,
                            "Failed to send OTP: ${error.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error sending OTP", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnSendOtp.isEnabled = true
                progressSendOtp.visibility = View.GONE
            }
        }
    }

    private fun verifyOtp() {
        val otpCode = getOtpCode()
        if (otpCode.length != 6) {
            Toast.makeText(this, "Please enter the complete OTP", Toast.LENGTH_SHORT).show()
            return
        }

        val requestId = otpRequestId
        if (requestId == null) {
            Toast.makeText(this, "Session expired. Please request a new OTP.", Toast.LENGTH_LONG).show()
            showPhoneNumberStep()
            return
        }

        // Show loading
        btnVerifyOtp.isEnabled = false
        progressVerifyOtp.visibility = View.VISIBLE

        coroutineScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.verifyOtp(currentPhoneNumber, otpCode, requestId)
                }

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "OTP verified successfully")
                        
                        // Save tokens
                        tokenManager.saveAccessToken(response.accessToken)
                        response.refreshToken?.let { tokenManager.saveRefreshToken(it) }
                        
                        // Mark user as logged in
                        HealthPayKeyboardApplication.setLoggedIn(true)
                        HealthPayKeyboardApplication.setPhoneNumber(currentPhoneNumber)
                        
                        // Proceed to biometric setup
                        proceedToBiometricSetup()
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to verify OTP", error)
                        Toast.makeText(
                            this@LoginActivity,
                            "Invalid OTP. Please try again.",
                            Toast.LENGTH_LONG
                        ).show()
                        // Clear OTP fields
                        listOf(etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6).forEach { 
                            it.setText("") 
                        }
                        etOtp1.requestFocus()
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error verifying OTP", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                btnVerifyOtp.isEnabled = true
                progressVerifyOtp.visibility = View.GONE
            }
        }
    }

    private fun proceedToBiometricSetup() {
        // Launch BiometricAuthActivity to setup biometric authentication
        val intent = Intent(this, BiometricAuthActivity::class.java).apply {
            putExtra(BiometricAuthActivity.EXTRA_AUTH_MODE, BiometricAuthActivity.MODE_SETUP)
        }
        startActivityForResult(intent, REQUEST_BIOMETRIC_SETUP)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_BIOMETRIC_SETUP -> {
                // Whether biometric setup succeeded or was skipped, login is complete
                setResult(RESULT_LOGIN_SUCCESS)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (otpVerificationLayout.visibility == View.VISIBLE) {
            // Go back to phone number step
            showPhoneNumberStep()
        } else {
            // Cancel login
            setResult(RESULT_LOGIN_CANCELLED)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }

}
