package tech.healthpay.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R
import tech.healthpay.keyboard.api.*

/**
 * Login Activity - Handles OTP-based authentication
 * 
 * v1.2.2 - Added timeout handling to prevent UI from hanging
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val OTP_RESEND_DELAY_MS = 60000L
        private const val API_TIMEOUT_MS = 30000L // 30 second timeout
    }

    // UI Components
    private lateinit var mobileInputLayout: TextInputLayout
    private lateinit var mobileInput: TextInputEditText
    private lateinit var otpInputLayout: TextInputLayout
    private lateinit var otpInput: TextInputEditText
    private lateinit var sendOtpButton: Button
    private lateinit var verifyOtpButton: Button
    private lateinit var resendOtpButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var timerText: TextView

    // State
    private var currentRequestId: String = ""
    private var currentMobile: String = ""
    private var isOtpSent: Boolean = false
    private var resendTimer: CountDownTimer? = null
    private var apiTimeoutHandler: Handler? = null
    private var apiTimeoutRunnable: Runnable? = null
    private var isRequestInProgress = false

    private val apiClient: HealthPayApiClient by lazy { HealthPayKeyboardApplication.apiClient }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        apiTimeoutHandler = Handler(Looper.getMainLooper())
        
        initViews()
        setupListeners()
        showMobileInputState()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
        cancelApiTimeout()
    }

    private fun initViews() {
        mobileInputLayout = findViewById(R.id.mobile_input_layout)
        mobileInput = findViewById(R.id.mobile_input)
        otpInputLayout = findViewById(R.id.otp_input_layout)
        otpInput = findViewById(R.id.otp_input)
        sendOtpButton = findViewById(R.id.btn_send_otp)
        verifyOtpButton = findViewById(R.id.btn_verify_otp)
        resendOtpButton = findViewById(R.id.btn_resend_otp)
        progressBar = findViewById(R.id.progress_bar)
        statusText = findViewById(R.id.status_text)
        timerText = findViewById(R.id.timer_text)
    }

    private fun setupListeners() {
        mobileInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                sendOtpButton.isEnabled = isValidMobile(s?.toString() ?: "") && !isRequestInProgress
                mobileInputLayout.error = null
            }
        })

        otpInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                verifyOtpButton.isEnabled = (s?.length ?: 0) == 6 && !isRequestInProgress
                otpInputLayout.error = null
            }
        })

        sendOtpButton.setOnClickListener {
            val mobile = mobileInput.text?.toString()?.trim() ?: ""
            if (isValidMobile(mobile)) requestOtp(mobile)
            else mobileInputLayout.error = getString(R.string.error_invalid_mobile)
        }

        verifyOtpButton.setOnClickListener {
            val otp = otpInput.text?.toString()?.trim() ?: ""
            if (otp.length == 6) verifyOtp(otp)
            else otpInputLayout.error = getString(R.string.error_invalid_otp)
        }

        resendOtpButton.setOnClickListener { requestOtp(currentMobile) }
    }

    private fun isValidMobile(mobile: String): Boolean {
        return "^01[0125][0-9]{8}$".toRegex().matches(mobile)
    }

    private fun formatMobileForApi(mobile: String): String {
        return if (mobile.startsWith("01")) "+2$mobile" else mobile
    }

    // =====================
    // Timeout Management
    // =====================

    private fun startApiTimeout(operation: String) {
        cancelApiTimeout()
        
        apiTimeoutRunnable = Runnable {
            if (isRequestInProgress) {
                Log.e(TAG, "$operation timed out after ${API_TIMEOUT_MS}ms")
                isRequestInProgress = false
                runOnUiThread {
                    hideLoading()
                    showTimeoutError(operation)
                }
            }
        }
        
        apiTimeoutHandler?.postDelayed(apiTimeoutRunnable!!, API_TIMEOUT_MS)
    }

    private fun cancelApiTimeout() {
        apiTimeoutRunnable?.let { apiTimeoutHandler?.removeCallbacks(it) }
        apiTimeoutRunnable = null
    }

    private fun showTimeoutError(operation: String) {
        showErrorDialog(
            title = getString(R.string.error_timeout_title),
            message = getString(R.string.error_timeout_message),
            retryAction = {
                if (operation == "OTP_REQUEST") {
                    requestOtp(currentMobile)
                } else {
                    verifyOtp(otpInput.text?.toString() ?: "")
                }
            }
        )
        
        // Re-enable inputs
        if (!isOtpSent) {
            mobileInput.isEnabled = true
            sendOtpButton.isEnabled = isValidMobile(mobileInput.text?.toString() ?: "")
        } else {
            otpInput.isEnabled = true
            verifyOtpButton.isEnabled = (otpInput.text?.length ?: 0) == 6
            resendOtpButton.isEnabled = true
        }
    }

    // =====================
    // OTP Request
    // =====================

    private fun requestOtp(mobile: String) {
        if (isRequestInProgress) {
            Log.w(TAG, "Request already in progress, ignoring")
            return
        }
        
        Log.d(TAG, "Requesting OTP for: ${mobile.takeLast(4)}")
        currentMobile = mobile
        isRequestInProgress = true
        showLoading(getString(R.string.status_sending_otp))
        startApiTimeout("OTP_REQUEST")
        
        apiClient.requestOtp(formatMobileForApi(mobile), object : ApiCallback<OtpResponse> {
            override fun onSuccess(response: OtpResponse) {
                cancelApiTimeout()
                isRequestInProgress = false
                runOnUiThread {
                    Log.d(TAG, "OTP sent successfully")
                    currentRequestId = response.requestId
                    isOtpSent = true
                    hideLoading()
                    showOtpInputState()
                    showSuccess(getString(R.string.status_otp_sent))
                    startResendTimer()
                }
            }

            override fun onError(error: ApiError) {
                cancelApiTimeout()
                isRequestInProgress = false
                runOnUiThread {
                    Log.e(TAG, "OTP request failed: ${error.code} - ${error.message}")
                    hideLoading()
                    showError(error)
                    mobileInput.isEnabled = true
                    sendOtpButton.isEnabled = isValidMobile(mobileInput.text?.toString() ?: "")
                }
            }
        })
    }

    // =====================
    // OTP Verification
    // =====================

    private fun verifyOtp(otp: String) {
        if (isRequestInProgress) {
            Log.w(TAG, "Request already in progress, ignoring")
            return
        }
        
        Log.d(TAG, "Verifying OTP")
        isRequestInProgress = true
        showLoading(getString(R.string.status_verifying))
        startApiTimeout("OTP_VERIFY")
        
        apiClient.verifyOtp(formatMobileForApi(currentMobile), otp, currentRequestId, object : ApiCallback<AuthResponse> {
            override fun onSuccess(response: AuthResponse) {
                cancelApiTimeout()
                isRequestInProgress = false
                runOnUiThread {
                    Log.d(TAG, "OTP verified successfully")
                    hideLoading()
                    showSuccess(getString(R.string.status_login_success))
                    navigateToMain()
                }
            }

            override fun onError(error: ApiError) {
                cancelApiTimeout()
                isRequestInProgress = false
                runOnUiThread {
                    Log.e(TAG, "OTP verification failed: ${error.code}")
                    hideLoading()
                    when (error.code) {
                        ApiErrorCode.UNAUTHORIZED, ApiErrorCode.VALIDATION_ERROR -> {
                            otpInputLayout.error = getString(R.string.error_invalid_otp)
                            otpInput.text?.clear()
                            otpInput.isEnabled = true
                            verifyOtpButton.isEnabled = false
                        }
                        else -> showError(error)
                    }
                }
            }
        })
    }

    // =====================
    // UI State Management
    // =====================

    private fun showMobileInputState() {
        mobileInputLayout.visibility = View.VISIBLE
        mobileInput.isEnabled = true
        sendOtpButton.visibility = View.VISIBLE
        otpInputLayout.visibility = View.GONE
        verifyOtpButton.visibility = View.GONE
        resendOtpButton.visibility = View.GONE
        timerText.visibility = View.GONE
    }

    private fun showOtpInputState() {
        mobileInputLayout.visibility = View.VISIBLE
        mobileInput.isEnabled = false
        sendOtpButton.visibility = View.GONE
        otpInputLayout.visibility = View.VISIBLE
        otpInput.text?.clear()
        otpInput.isEnabled = true
        otpInput.requestFocus()
        verifyOtpButton.visibility = View.VISIBLE
        verifyOtpButton.isEnabled = false
        resendOtpButton.visibility = View.VISIBLE
        resendOtpButton.isEnabled = false
        timerText.visibility = View.VISIBLE
    }

    private fun showLoading(message: String) {
        progressBar.visibility = View.VISIBLE
        statusText.text = message
        statusText.visibility = View.VISIBLE
        statusText.setTextColor(getColor(R.color.text_secondary))
        sendOtpButton.isEnabled = false
        verifyOtpButton.isEnabled = false
        resendOtpButton.isEnabled = false
        mobileInput.isEnabled = false
        otpInput.isEnabled = false
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        statusText.text = message
        statusText.setTextColor(getColor(R.color.success_green))
        statusText.visibility = View.VISIBLE
    }

    private fun showError(error: ApiError) {
        when (error.code) {
            ApiErrorCode.NO_INTERNET -> {
                showErrorDialog(
                    getString(R.string.error_no_internet_title),
                    getString(R.string.error_no_internet_message)
                ) { retryCurrentOperation() }
            }
            ApiErrorCode.TIMEOUT, ApiErrorCode.SERVER_UNREACHABLE -> {
                showErrorDialog(
                    getString(R.string.error_timeout_title),
                    getString(R.string.error_timeout_message)
                ) { retryCurrentOperation() }
            }
            ApiErrorCode.SERVER_ERROR -> {
                showErrorDialog(
                    getString(R.string.error_server_title),
                    getString(R.string.error_server_message)
                ) { retryCurrentOperation() }
            }
            ApiErrorCode.RATE_LIMITED -> {
                showErrorDialog(
                    getString(R.string.error_rate_limited_title),
                    error.message,
                    null
                )
            }
            else -> {
                if (!isOtpSent) {
                    mobileInputLayout.error = error.message
                } else {
                    otpInputLayout.error = error.message
                }
            }
        }
    }

    private fun retryCurrentOperation() {
        if (!isOtpSent) {
            requestOtp(currentMobile)
        } else {
            val otp = otpInput.text?.toString() ?: ""
            if (otp.length == 6) verifyOtp(otp)
        }
    }

    private fun showErrorDialog(title: String, message: String, retryAction: (() -> Unit)?) {
        AlertDialog.Builder(this).apply {
            setTitle(title)
            setMessage(message)
            setCancelable(true)
            if (retryAction != null) {
                setPositiveButton(R.string.btn_retry) { _, _ -> retryAction() }
                setNegativeButton(R.string.btn_cancel) { dialog, _ -> 
                    dialog.dismiss()
                    // Re-enable inputs on cancel
                    if (!isOtpSent) {
                        mobileInput.isEnabled = true
                        sendOtpButton.isEnabled = isValidMobile(mobileInput.text?.toString() ?: "")
                    } else {
                        otpInput.isEnabled = true
                        verifyOtpButton.isEnabled = (otpInput.text?.length ?: 0) == 6
                        resendOtpButton.isEnabled = true
                    }
                }
            } else {
                setPositiveButton(android.R.string.ok) { dialog, _ ->
                    dialog.dismiss()
                    if (!isOtpSent) {
                        mobileInput.isEnabled = true
                        sendOtpButton.isEnabled = isValidMobile(mobileInput.text?.toString() ?: "")
                    }
                }
            }
        }.show()
    }

    private fun startResendTimer() {
        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(OTP_RESEND_DELAY_MS, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timerText.text = getString(R.string.timer_resend_otp, millisUntilFinished / 1000)
                resendOtpButton.isEnabled = false
            }
            override fun onFinish() {
                timerText.text = ""
                resendOtpButton.isEnabled = !isRequestInProgress
            }
        }.start()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
