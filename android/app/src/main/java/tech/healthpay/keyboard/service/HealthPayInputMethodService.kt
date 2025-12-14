package tech.healthpay.keyboard.service

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import tech.healthpay.keyboard.R

/**
 * HealthPay Keyboard Input Method Service
 * 
 * v1.2.1 - Fixed shift key crash with null-safety and resource validation
 */
class HealthPayInputMethodService : InputMethodService() {

    companion object {
        private const val TAG = "HealthPayKeyboard"
    }

    private var keyboardView: View? = null
    private var qwertyLayout: LinearLayout? = null
    private var shiftButton: ImageButton? = null
    private var backspaceButton: ImageButton? = null
    private var spaceButton: Button? = null
    private var enterButton: ImageButton? = null
    
    private var isShiftActive = false
    private var isCapsLocked = false
    private var lastShiftClickTime = 0L

    override fun onCreateInputView(): View {
        Log.d(TAG, "Creating input view")
        keyboardView = layoutInflater.inflate(R.layout.keyboard_qwerty, null)
        initializeViews()
        setupKeyListeners()
        return keyboardView!!
    }

    private fun initializeViews() {
        keyboardView?.let { view ->
            qwertyLayout = view.findViewById(R.id.qwerty_layout)
            shiftButton = view.findViewById(R.id.btn_shift)
            backspaceButton = view.findViewById(R.id.btn_backspace)
            spaceButton = view.findViewById(R.id.btn_space)
            enterButton = view.findViewById(R.id.btn_enter)
        }
    }

    private fun setupKeyListeners() {
        setupLetterKeys()
        
        // Shift button with double-tap for caps lock
        shiftButton?.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastShiftClickTime < 300) {
                isCapsLocked = !isCapsLocked
                isShiftActive = isCapsLocked
                Log.d(TAG, "Caps lock ${if (isCapsLocked) "ON" else "OFF"}")
            } else {
                if (!isCapsLocked) isShiftActive = !isShiftActive
            }
            lastShiftClickTime = currentTime
            updateShiftKeyUI()
            updateKeyLabels()
        }
        
        // Backspace
        backspaceButton?.setOnClickListener {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }
        backspaceButton?.setOnLongClickListener {
            currentInputConnection?.deleteSurroundingText(10, 0)
            true
        }
        
        // Space
        spaceButton?.setOnClickListener { commitText(" ") }
        
        // Enter
        enterButton?.setOnClickListener {
            val ic = currentInputConnection ?: return@setOnClickListener
            val ei = currentInputEditorInfo
            when (ei?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)) {
                EditorInfo.IME_ACTION_SEARCH -> ic.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
                EditorInfo.IME_ACTION_GO -> ic.performEditorAction(EditorInfo.IME_ACTION_GO)
                EditorInfo.IME_ACTION_SEND -> ic.performEditorAction(EditorInfo.IME_ACTION_SEND)
                EditorInfo.IME_ACTION_NEXT -> ic.performEditorAction(EditorInfo.IME_ACTION_NEXT)
                EditorInfo.IME_ACTION_DONE -> ic.performEditorAction(EditorInfo.IME_ACTION_DONE)
                else -> commitText("\n")
            }
        }
        
        // Numbers button
        keyboardView?.findViewById<Button>(R.id.btn_numbers)?.setOnClickListener {
            // TODO: Switch to numbers layout
        }
        
        // Comma and period
        keyboardView?.findViewById<Button>(R.id.btn_comma)?.setOnClickListener { commitText(",") }
        keyboardView?.findViewById<Button>(R.id.btn_period)?.setOnClickListener { commitText(".") }
    }

    private fun setupLetterKeys() {
        val letterKeys = listOf(
            R.id.key_q, R.id.key_w, R.id.key_e, R.id.key_r, R.id.key_t,
            R.id.key_y, R.id.key_u, R.id.key_i, R.id.key_o, R.id.key_p,
            R.id.key_a, R.id.key_s, R.id.key_d, R.id.key_f, R.id.key_g,
            R.id.key_h, R.id.key_j, R.id.key_k, R.id.key_l,
            R.id.key_z, R.id.key_x, R.id.key_c, R.id.key_v,
            R.id.key_b, R.id.key_n, R.id.key_m
        )
        
        letterKeys.forEach { keyId ->
            keyboardView?.findViewById<Button>(keyId)?.let { button ->
                button.setOnClickListener {
                    val letter = button.text.toString()
                    val finalLetter = if (isShiftActive || isCapsLocked) letter.uppercase() else letter.lowercase()
                    commitText(finalLetter)
                    
                    if (isShiftActive && !isCapsLocked) {
                        isShiftActive = false
                        updateShiftKeyUI()
                        updateKeyLabels()
                    }
                }
            }
        }
    }

    /**
     * Updates the shift key icon based on current state
     * v1.2.1 - Added null-safety and try-catch for resource handling
     */
    private fun updateShiftKeyUI() {
        shiftButton?.let { button ->
            try {
                val iconRes = when {
                    isCapsLocked -> R.drawable.ic_shift_locked
                    isShiftActive -> R.drawable.ic_shift_active
                    else -> R.drawable.ic_shift
                }
                button.setImageResource(iconRes)
                Log.d(TAG, "Shift UI updated: capsLock=$isCapsLocked, shift=$isShiftActive")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating shift key UI", e)
                // Fallback - change tint color
                try {
                    val tintColor = if (isShiftActive || isCapsLocked) {
                        resources.getColor(R.color.healthpay_primary, theme)
                    } else {
                        resources.getColor(R.color.key_text_color, theme)
                    }
                    button.setColorFilter(tintColor)
                } catch (e2: Exception) {
                    Log.e(TAG, "Fallback tint also failed", e2)
                }
            }
        } ?: Log.w(TAG, "Shift button is null")
    }

    private fun updateKeyLabels() {
        val letterKeys = mapOf(
            R.id.key_q to "q", R.id.key_w to "w", R.id.key_e to "e",
            R.id.key_r to "r", R.id.key_t to "t", R.id.key_y to "y",
            R.id.key_u to "u", R.id.key_i to "i", R.id.key_o to "o",
            R.id.key_p to "p", R.id.key_a to "a", R.id.key_s to "s",
            R.id.key_d to "d", R.id.key_f to "f", R.id.key_g to "g",
            R.id.key_h to "h", R.id.key_j to "j", R.id.key_k to "k",
            R.id.key_l to "l", R.id.key_z to "z", R.id.key_x to "x",
            R.id.key_c to "c", R.id.key_v to "v", R.id.key_b to "b",
            R.id.key_n to "n", R.id.key_m to "m"
        )
        
        letterKeys.forEach { (keyId, letter) ->
            keyboardView?.findViewById<Button>(keyId)?.text = 
                if (isShiftActive || isCapsLocked) letter.uppercase() else letter.lowercase()
        }
    }

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(TAG, "Input view started")
        if (!isCapsLocked) isShiftActive = false
        updateShiftKeyUI()
        updateKeyLabels()
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.d(TAG, "Input view finished")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")
        keyboardView = null
    }
}
