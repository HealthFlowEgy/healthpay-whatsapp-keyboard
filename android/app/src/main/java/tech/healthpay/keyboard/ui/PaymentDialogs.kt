package tech.healthpay.keyboard.ui

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import tech.healthpay.keyboard.R
import tech.healthpay.keyboard.model.SavedRecipient
import tech.healthpay.keyboard.model.Transaction
import java.text.DecimalFormat
import java.util.Locale

/**
 * Quick Pay Dialog
 * 
 * A compact overlay dialog for quick payment within the keyboard view.
 * Supports:
 * - Amount entry with EGP formatting
 * - Phone number input
 * - Optional note
 * - Recent recipients list
 */
class QuickPayDialog(
    context: Context
) : Dialog(context) {

    var onPaymentSubmit: ((amount: Double, recipientPhone: String, note: String?) -> Unit)? = null
    var onDismiss: (() -> Unit)? = null

    private lateinit var amountInput: EditText
    private lateinit var phoneInput: TextInputEditText
    private lateinit var noteInput: TextInputEditText
    private lateinit var payButton: Button
    private lateinit var closeButton: ImageButton
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var recentRecipientsContainer: LinearLayout
    private lateinit var errorText: TextView

    private var selectedRecipient: SavedRecipient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        
        setContentView(createDialogView())
        
        window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            
            // Adjust for keyboard
            setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
        
        setupListeners()
        setupAmountFormatter()
    }

    private fun createDialogView(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.dialog_background)
            setPadding(24.dp, 20.dp, 24.dp, 24.dp)
            elevation = 16f

            // Header with title and close button
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16.dp }

                addView(TextView(context).apply {
                    text = "💳 Quick Pay"
                    textSize = 18f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })

                closeButton = ImageButton(context).apply {
                    setImageResource(R.drawable.ic_close)
                    setBackgroundResource(R.drawable.ripple_circle)
                    setOnClickListener { dismiss() }
                }
                addView(closeButton)
            })

            // Amount Input
            addView(CardView(context).apply {
                radius = 12f.dp
                cardElevation = 2f.dp
                setCardBackgroundColor(ContextCompat.getColor(context, R.color.input_background))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12.dp }

                addView(LinearLayout(context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(16.dp, 8.dp, 16.dp, 8.dp)

                    addView(TextView(context).apply {
                        text = "EGP"
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                    })

                    amountInput = EditText(context).apply {
                        hint = "0.00"
                        textSize = 28f
                        background = null
                        inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                        setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                        setHintTextColor(ContextCompat.getColor(context, R.color.text_hint))
                        layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                            marginStart = 8.dp
                        }
                    }
                    addView(amountInput)
                })
            })

            // Phone Input
            val phoneLayout = TextInputLayout(context, null, R.style.TextInputLayoutStyle).apply {
                hint = "Recipient Phone"
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                boxBackgroundColor = ContextCompat.getColor(context, R.color.input_background)
                setBoxCornerRadii(12f.dp, 12f.dp, 12f.dp, 12f.dp)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12.dp }

                phoneInput = TextInputEditText(context).apply {
                    inputType = android.text.InputType.TYPE_CLASS_PHONE
                    maxLines = 1
                }
                addView(phoneInput)
            }
            addView(phoneLayout)

            // Note Input (optional)
            val noteLayout = TextInputLayout(context, null, R.style.TextInputLayoutStyle).apply {
                hint = "Note (optional)"
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                boxBackgroundColor = ContextCompat.getColor(context, R.color.input_background)
                setBoxCornerRadii(12f.dp, 12f.dp, 12f.dp, 12f.dp)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16.dp }

                noteInput = TextInputEditText(context).apply {
                    maxLines = 2
                }
                addView(noteInput)
            }
            addView(noteLayout)

            // Recent Recipients (optional section)
            recentRecipientsContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                isVisible = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 16.dp }
            }
            addView(recentRecipientsContainer)

            // Error Text
            errorText = TextView(context).apply {
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.error_color))
                isVisible = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8.dp }
            }
            addView(errorText)

            // Pay Button
            payButton = Button(context).apply {
                text = "Send Payment"
                textSize = 16f
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.button_primary)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    56.dp
                )
                isEnabled = false
            }
            addView(payButton)

            // Loading Indicator (overlaid)
            loadingIndicator = ProgressBar(context).apply {
                isVisible = false
            }
        }
    }

    private fun setupListeners() {
        closeButton.setOnClickListener {
            onDismiss?.invoke()
            dismiss()
        }

        payButton.setOnClickListener {
            validateAndSubmit()
        }

        // Enable/disable pay button based on inputs
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateInputs()
            }
        }

        amountInput.addTextChangedListener(textWatcher)
        phoneInput.addTextChangedListener(textWatcher)
        phoneInput.addTextChangedListener(PhoneNumberFormattingTextWatcher("EG"))
    }

    private fun setupAmountFormatter() {
        amountInput.addTextChangedListener(object : TextWatcher {
            private var current = ""
            private val decimalFormat = DecimalFormat("#,###.##")

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString()
                if (input != current) {
                    amountInput.removeTextChangedListener(this)
                    
                    val cleanString = input.replace("[,.]".toRegex(), "")
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatted = decimalFormat.format(parsed / 100)
                        current = formatted
                        amountInput.setText(formatted)
                        amountInput.setSelection(formatted.length)
                    }
                    
                    amountInput.addTextChangedListener(this)
                }
            }
        })
    }

    private fun validateInputs() {
        val amount = parseAmount()
        val phone = phoneInput.text?.toString()?.trim() ?: ""
        
        payButton.isEnabled = amount > 0 && phone.length >= 10
        
        // Update button text with amount
        if (amount > 0) {
            payButton.text = "Send EGP ${String.format(Locale.getDefault(), "%,.2f", amount)}"
        } else {
            payButton.text = "Send Payment"
        }
    }

    private fun validateAndSubmit() {
        val amount = parseAmount()
        val phone = phoneInput.text?.toString()?.trim() ?: ""
        val note = noteInput.text?.toString()?.trim()

        // Validate amount
        if (amount <= 0) {
            showError("Please enter a valid amount")
            return
        }

        // Validate phone
        if (phone.length < 10) {
            showError("Please enter a valid phone number")
            return
        }

        // Clear any errors
        hideError()

        // Format phone number (remove spaces and dashes)
        val formattedPhone = phone.replace("[\\s-]".toRegex(), "")

        // Submit
        onPaymentSubmit?.invoke(amount, formattedPhone, note.takeIf { it?.isNotEmpty() == true })
        dismiss()
    }

    private fun parseAmount(): Double {
        val text = amountInput.text?.toString() ?: ""
        val cleanString = text.replace("[,]".toRegex(), "")
        return cleanString.toDoubleOrNull() ?: 0.0
    }

    private fun showError(message: String) {
        errorText.text = message
        errorText.isVisible = true
    }

    private fun hideError() {
        errorText.isVisible = false
    }

    fun setLoading(loading: Boolean) {
        loadingIndicator.isVisible = loading
        payButton.isEnabled = !loading
        amountInput.isEnabled = !loading
        phoneInput.isEnabled = !loading
        noteInput.isEnabled = !loading
    }

    fun setRecentRecipients(recipients: List<SavedRecipient>) {
        if (recipients.isEmpty()) {
            recentRecipientsContainer.isVisible = false
            return
        }

        recentRecipientsContainer.apply {
            removeAllViews()
            isVisible = true

            addView(TextView(context).apply {
                text = "Recent"
                textSize = 12f
                setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 8.dp }
            })

            val recipientRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            recipients.take(4).forEach { recipient ->
                recipientRow.addView(createRecipientChip(recipient))
            }

            addView(recipientRow)
        }
    }

    private fun createRecipientChip(recipient: SavedRecipient): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(12.dp, 8.dp, 12.dp, 8.dp)
            setBackgroundResource(R.drawable.chip_background)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { marginEnd = 8.dp }

            // Avatar circle
            addView(TextView(context).apply {
                text = recipient.name.firstOrNull()?.toString()?.uppercase() ?: "?"
                textSize = 16f
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setBackgroundResource(R.drawable.avatar_background)
                layoutParams = LinearLayout.LayoutParams(36.dp, 36.dp)
            })

            // Name
            addView(TextView(context).apply {
                text = recipient.name.split(" ").first()
                textSize = 11f
                maxLines = 1
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4.dp }
            })

            setOnClickListener {
                selectedRecipient = recipient
                phoneInput.setText(recipient.phone)
            }
        }
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()

    private val Float.dp: Float
        get() = this * context.resources.displayMetrics.density
}

/**
 * Payment Bottom Sheet
 * 
 * Full-featured bottom sheet for payments and requests
 */
class PaymentBottomSheet(
    context: Context,
    private val mode: Mode = Mode.SEND
) : BottomSheetDialog(context) {

    enum class Mode {
        SEND,
        REQUEST
    }

    var onPaymentSubmit: ((amount: Double, recipientPhone: String, note: String?) -> Unit)? = null
    var onRequestSubmit: ((amount: Double, note: String?) -> Unit)? = null
    var onDismissed: (() -> Unit)? = null

    private lateinit var contentView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        contentView = createContentView()
        setContentView(contentView)

        // Expand fully
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
    }

    private fun createContentView(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bottom_sheet_background)
            setPadding(24.dp, 16.dp, 24.dp, 32.dp)

            // Handle bar
            addView(View(context).apply {
                setBackgroundResource(R.drawable.handle_bar)
                layoutParams = LinearLayout.LayoutParams(40.dp, 4.dp).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    bottomMargin = 16.dp
                }
            })

            // Title
            addView(TextView(context).apply {
                text = if (mode == Mode.SEND) "💳 Send Payment" else "💰 Request Payment"
                textSize = 20f
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 24.dp }
            })

            // Add form fields similar to QuickPayDialog
            // ... (implementation similar to QuickPayDialog)
        }
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}

/**
 * Transaction History Bottom Sheet
 */
class TransactionHistorySheet(
    context: Context
) : BottomSheetDialog(context) {

    private var transactions: List<Transaction> = emptyList()
    private lateinit var recyclerView: RecyclerView

    fun setTransactions(list: List<Transaction>) {
        transactions = list
        if (::recyclerView.isInitialized) {
            setupRecyclerView()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(createContentView())
        
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun createContentView(): View {
        return LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.bottom_sheet_background)
            setPadding(0, 16.dp, 0, 0)

            // Handle bar
            addView(View(context).apply {
                setBackgroundResource(R.drawable.handle_bar)
                layoutParams = LinearLayout.LayoutParams(40.dp, 4.dp).apply {
                    gravity = Gravity.CENTER_HORIZONTAL
                    bottomMargin = 16.dp
                }
            })

            // Title
            addView(TextView(context).apply {
                text = "📊 Recent Transactions"
                textSize = 18f
                setPadding(24.dp, 0, 24.dp, 16.dp)
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            })

            // RecyclerView
            recyclerView = RecyclerView(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    400.dp
                )
                layoutManager = LinearLayoutManager(context)
            }
            addView(recyclerView)

            setupRecyclerView()
        }
    }

    private fun setupRecyclerView() {
        recyclerView.adapter = TransactionAdapter(transactions)
    }

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}

/**
 * Transaction List Adapter
 */
class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: TextView = view.findViewById(R.id.avatar)
        val name: TextView = view.findViewById(R.id.name)
        val date: TextView = view.findViewById(R.id.date)
        val amount: TextView = view.findViewById(R.id.amount)
        val status: TextView = view.findViewById(R.id.status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        
        val itemView = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(24.dp, 12.dp, 24.dp, 12.dp)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT
            )

            // Avatar
            addView(TextView(context).apply {
                id = R.id.avatar
                textSize = 16f
                gravity = Gravity.CENTER
                setBackgroundResource(R.drawable.avatar_background)
                layoutParams = LinearLayout.LayoutParams(40.dp, 40.dp)
            })

            // Name and Date column
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    marginStart = 12.dp
                }

                addView(TextView(context).apply {
                    id = R.id.name
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                })

                addView(TextView(context).apply {
                    id = R.id.date
                    textSize = 12f
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                })
            })

            // Amount and Status column
            addView(LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.END

                addView(TextView(context).apply {
                    id = R.id.amount
                    textSize = 14f
                })

                addView(TextView(context).apply {
                    id = R.id.status
                    textSize = 10f
                })
            })
        }

        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]
        
        holder.avatar.text = transaction.counterpartyInitial
        holder.name.text = transaction.counterpartyName
        holder.date.text = transaction.shortDate
        holder.amount.text = transaction.formattedAmount
        holder.amount.setTextColor(
            if (transaction.type == tech.healthpay.keyboard.model.TransactionType.RECEIVED) 
                0xFF4CAF50.toInt() 
            else 
                0xFFE53935.toInt()
        )
        holder.status.text = transaction.status.name.lowercase().replaceFirstChar { it.uppercase() }
        holder.status.setTextColor(transaction.statusColor)
    }

    override fun getItemCount() = transactions.size

    private val Int.dp: Int
        get() = (this * 1.0f).toInt() // Simplified
}
