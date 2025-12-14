import UIKit
import Security
import LocalAuthentication

/// HealthPay Keyboard Extension Controller
/// 
/// This UIInputViewController provides a custom keyboard with integrated
/// HealthPay wallet functionality for WhatsApp and other messaging apps.
class HealthPayKeyboardViewController: UIInputViewController {
    
    // MARK: - Properties
    
    private var keyboardView: HealthPayKeyboardView!
    private let walletService = WalletService.shared
    private let authManager = AuthenticationManager.shared
    private let biometricHelper = BiometricHelper()
    
    private var isWhatsAppContext = false
    private var isAuthenticated = false
    
    // MARK: - Lifecycle
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        setupKeyboardView()
        checkAuthenticationState()
        observeNotifications()
    }
    
    override func viewWillAppear(_ animated: Bool) {
        super.viewWillAppear(animated)
        
        // Update context detection
        detectAppContext()
        
        // Refresh wallet data if authenticated
        if authManager.isAuthenticated {
            refreshWalletData()
        }
    }
    
    override func textWillChange(_ textInput: UITextInput?) {
        super.textWillChange(textInput)
    }
    
    override func textDidChange(_ textInput: UITextInput?) {
        super.textDidChange(textInput)
    }
    
    // MARK: - Setup
    
    private func setupKeyboardView() {
        keyboardView = HealthPayKeyboardView(frame: view.bounds)
        keyboardView.delegate = self
        keyboardView.translatesAutoresizingMaskIntoConstraints = false
        
        view.addSubview(keyboardView)
        
        NSLayoutConstraint.activate([
            keyboardView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            keyboardView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            keyboardView.topAnchor.constraint(equalTo: view.topAnchor),
            keyboardView.bottomAnchor.constraint(equalTo: view.bottomAnchor)
        ])
        
        // Set initial keyboard height
        updateKeyboardHeight()
    }
    
    private func updateKeyboardHeight() {
        let heightConstraint = view.heightAnchor.constraint(equalToConstant: 300)
        heightConstraint.priority = .defaultHigh
        heightConstraint.isActive = true
    }
    
    private func detectAppContext() {
        // Try to detect if we're in WhatsApp
        // Note: This is limited due to sandboxing
        if let hostBundle = self.textDocumentProxy.documentContextBeforeInput {
            // Limited context available
        }
        
        // For now, we'll enable payment features by default
        // In production, you might use other heuristics
        isWhatsAppContext = true
        keyboardView.setPaymentFeaturesEnabled(isWhatsAppContext && isAuthenticated)
    }
    
    private func checkAuthenticationState() {
        isAuthenticated = authManager.isAuthenticated
        keyboardView.setAuthenticationState(isAuthenticated)
        
        if isAuthenticated {
            refreshWalletData()
        }
    }
    
    private func refreshWalletData() {
        Task {
            do {
                let balance = try await walletService.getBalance()
                await MainActor.run {
                    keyboardView.updateBalance(balance)
                }
            } catch {
                print("Failed to refresh balance: \(error)")
            }
        }
    }
    
    private func observeNotifications() {
        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleAuthStateChanged),
            name: .authStateChanged,
            object: nil
        )
    }
    
    @objc private func handleAuthStateChanged() {
        checkAuthenticationState()
    }
    
    // MARK: - Payment Actions
    
    private func handlePayAction() {
        guard authManager.isAuthenticated else {
            showAuthenticationRequired()
            return
        }
        
        biometricHelper.authenticate(
            reason: "Authenticate to make payment"
        ) { [weak self] success, error in
            if success {
                self?.showPaymentSheet()
            } else {
                self?.showError("Authentication failed")
            }
        }
    }
    
    private func showPaymentSheet() {
        // Create and present payment sheet
        // Note: iOS keyboard extensions have limited UI capabilities
        // We'll insert a payment prompt message
        
        let promptMessage = """
        💳 To send payment:
        1. Open HealthPay app
        2. Tap Send Money
        3. Or use this link: healthpay://pay
        """
        
        textDocumentProxy.insertText(promptMessage)
    }
    
    private func handleRequestPayment() {
        guard authManager.isAuthenticated else {
            showAuthenticationRequired()
            return
        }
        
        // Generate payment request link
        Task {
            do {
                let link = try await walletService.generatePaymentLink(amount: nil, description: nil)
                await MainActor.run {
                    insertPaymentRequest(link: link)
                }
            } catch {
                await MainActor.run {
                    showError("Failed to generate payment link")
                }
            }
        }
    }
    
    private func insertPaymentRequest(link: String) {
        let message = """
        💰 *Payment Request*
        ━━━━━━━━━━━━━━━━
        
        👉 Tap to pay: \(link)
        
        Powered by HealthPay
        """
        
        textDocumentProxy.insertText(message)
    }
    
    private func handleWalletAction() {
        guard authManager.isAuthenticated else {
            showAuthenticationRequired()
            return
        }
        
        biometricHelper.authenticate(reason: "View wallet balance") { [weak self] success, _ in
            if success {
                self?.showWalletBalance()
            }
        }
    }
    
    private func showWalletBalance() {
        Task {
            do {
                let balance = try await walletService.getBalance()
                await MainActor.run {
                    // Show balance in a toast/popup
                    keyboardView.showBalanceCard(balance)
                }
            } catch {
                await MainActor.run {
                    showError("Failed to load balance")
                }
            }
        }
    }
    
    private func showAuthenticationRequired() {
        // Show login prompt
        keyboardView.showAuthPrompt { [weak self] in
            self?.openMainApp()
        }
    }
    
    private func openMainApp() {
        // Open the main HealthPay app for authentication
        if let url = URL(string: "healthpay://auth") {
            self.extensionContext?.open(url, completionHandler: nil)
        }
    }
    
    private func showError(_ message: String) {
        keyboardView.showError(message)
    }
}

// MARK: - HealthPayKeyboardViewDelegate

extension HealthPayKeyboardViewController: HealthPayKeyboardViewDelegate {
    
    func keyboardView(_ view: HealthPayKeyboardView, didTapKey key: String) {
        switch key {
        case "⌫":
            textDocumentProxy.deleteBackward()
        case "↵":
            textDocumentProxy.insertText("\n")
        case " ":
            textDocumentProxy.insertText(" ")
        case "🌐":
            advanceToNextInputMode()
        case "💳":
            handlePayAction()
        default:
            textDocumentProxy.insertText(key)
        }
        
        // Haptic feedback
        UIImpactFeedbackGenerator(style: .light).impactOccurred()
    }
    
    func keyboardViewDidTapPay(_ view: HealthPayKeyboardView) {
        handlePayAction()
    }
    
    func keyboardViewDidTapRequest(_ view: HealthPayKeyboardView) {
        handleRequestPayment()
    }
    
    func keyboardViewDidTapBalance(_ view: HealthPayKeyboardView) {
        handleWalletAction()
    }
    
    func keyboardViewDidTapQR(_ view: HealthPayKeyboardView) {
        // QR functionality requires opening main app
        if let url = URL(string: "healthpay://qr") {
            self.extensionContext?.open(url, completionHandler: nil)
        }
    }
    
    func keyboardViewDidTapSettings(_ view: HealthPayKeyboardView) {
        if let url = URL(string: "healthpay://settings") {
            self.extensionContext?.open(url, completionHandler: nil)
        }
    }
    
    func keyboardViewDidTapNextKeyboard(_ view: HealthPayKeyboardView) {
        advanceToNextInputMode()
    }
}

// MARK: - Notifications

extension Notification.Name {
    static let authStateChanged = Notification.Name("HealthPayAuthStateChanged")
}
