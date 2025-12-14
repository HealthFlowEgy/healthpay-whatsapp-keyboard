import UIKit

/// Delegate protocol for keyboard view events
protocol HealthPayKeyboardViewDelegate: AnyObject {
    func keyboardView(_ view: HealthPayKeyboardView, didTapKey key: String)
    func keyboardViewDidTapPay(_ view: HealthPayKeyboardView)
    func keyboardViewDidTapRequest(_ view: HealthPayKeyboardView)
    func keyboardViewDidTapBalance(_ view: HealthPayKeyboardView)
    func keyboardViewDidTapQR(_ view: HealthPayKeyboardView)
    func keyboardViewDidTapSettings(_ view: HealthPayKeyboardView)
    func keyboardViewDidTapNextKeyboard(_ view: HealthPayKeyboardView)
}

/// Custom keyboard view with payment toolbar
class HealthPayKeyboardView: UIView {
    
    // MARK: - Properties
    
    weak var delegate: HealthPayKeyboardViewDelegate?
    
    private var isShiftActive = false
    private var isCapsLock = false
    private var isArabic = false
    private var isAuthenticated = false
    private var paymentFeaturesEnabled = false
    
    private var currentBalance: WalletBalance?
    
    // MARK: - UI Components
    
    private lazy var toolbarView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(named: "ToolbarBackground") ?? .systemGray6
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    private lazy var payButton: UIButton = {
        let button = createToolbarButton(emoji: "💳", title: "Pay")
        button.addTarget(self, action: #selector(payTapped), for: .touchUpInside)
        return button
    }()
    
    private lazy var requestButton: UIButton = {
        let button = createToolbarButton(emoji: "💰", title: "Request")
        button.addTarget(self, action: #selector(requestTapped), for: .touchUpInside)
        return button
    }()
    
    private lazy var balanceView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(named: "BalanceCardBackground") ?? .white
        view.layer.cornerRadius = 8
        view.layer.shadowColor = UIColor.black.cgColor
        view.layer.shadowOpacity = 0.1
        view.layer.shadowOffset = CGSize(width: 0, height: 1)
        view.layer.shadowRadius = 2
        view.translatesAutoresizingMaskIntoConstraints = false
        
        let tapGesture = UITapGestureRecognizer(target: self, action: #selector(balanceTapped))
        view.addGestureRecognizer(tapGesture)
        
        return view
    }()
    
    private lazy var balanceLabel: UILabel = {
        let label = UILabel()
        label.text = "Login"
        label.font = .systemFont(ofSize: 14, weight: .medium)
        label.textColor = UIColor(named: "TextPrimary") ?? .label
        label.translatesAutoresizingMaskIntoConstraints = false
        return label
    }()
    
    private lazy var walletIcon: UIImageView = {
        let imageView = UIImageView()
        imageView.image = UIImage(systemName: "wallet.pass")
        imageView.tintColor = UIColor(named: "AccentColor") ?? .systemBlue
        imageView.contentMode = .scaleAspectFit
        imageView.translatesAutoresizingMaskIntoConstraints = false
        return imageView
    }()
    
    private lazy var qrButton: UIButton = {
        let button = createToolbarButton(emoji: "🔍", title: "QR")
        button.addTarget(self, action: #selector(qrTapped), for: .touchUpInside)
        return button
    }()
    
    private lazy var settingsButton: UIButton = {
        let button = UIButton(type: .system)
        button.setTitle("⚙️", for: .normal)
        button.titleLabel?.font = .systemFont(ofSize: 20)
        button.addTarget(self, action: #selector(settingsTapped), for: .touchUpInside)
        button.translatesAutoresizingMaskIntoConstraints = false
        return button
    }()
    
    private lazy var keyboardContainer: UIStackView = {
        let stack = UIStackView()
        stack.axis = .vertical
        stack.spacing = 6
        stack.distribution = .fillEqually
        stack.translatesAutoresizingMaskIntoConstraints = false
        return stack
    }()
    
    private lazy var authPromptView: UIView = {
        let view = UIView()
        view.backgroundColor = UIColor(named: "AuthPromptBackground") ?? .systemYellow.withAlphaComponent(0.2)
        view.isHidden = true
        view.translatesAutoresizingMaskIntoConstraints = false
        return view
    }()
    
    // Keyboard layouts
    private let englishKeys: [[String]] = [
        ["1", "2", "3", "4", "5", "6", "7", "8", "9", "0"],
        ["q", "w", "e", "r", "t", "y", "u", "i", "o", "p"],
        ["a", "s", "d", "f", "g", "h", "j", "k", "l"],
        ["⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"],
        ["123", "🌐", "💳", " ", ".", "↵"]
    ]
    
    private let englishKeysShift: [[String]] = [
        ["!", "@", "#", "$", "%", "^", "&", "*", "(", ")"],
        ["Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"],
        ["A", "S", "D", "F", "G", "H", "J", "K", "L"],
        ["⇧", "Z", "X", "C", "V", "B", "N", "M", "⌫"],
        ["123", "🌐", "💳", " ", ".", "↵"]
    ]
    
    private let arabicKeys: [[String]] = [
        ["١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠"],
        ["ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح"],
        ["ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م"],
        ["⇧", "ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "⌫"],
        ["123", "🌐", "💳", " ", ".", "↵"]
    ]
    
    // MARK: - Initialization
    
    override init(frame: CGRect) {
        super.init(frame: frame)
        setupUI()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupUI()
    }
    
    // MARK: - Setup
    
    private func setupUI() {
        backgroundColor = UIColor(named: "KeyboardBackground") ?? .systemGray5
        
        setupToolbar()
        setupAuthPrompt()
        setupKeyboard()
        
        buildKeyboard(keys: englishKeys)
    }
    
    private func setupToolbar() {
        addSubview(toolbarView)
        
        // Balance view with icon
        balanceView.addSubview(walletIcon)
        balanceView.addSubview(balanceLabel)
        
        let toolbarStack = UIStackView(arrangedSubviews: [
            payButton,
            requestButton,
            balanceView,
            qrButton,
            settingsButton
        ])
        toolbarStack.axis = .horizontal
        toolbarStack.spacing = 8
        toolbarStack.alignment = .center
        toolbarStack.translatesAutoresizingMaskIntoConstraints = false
        
        toolbarView.addSubview(toolbarStack)
        
        NSLayoutConstraint.activate([
            toolbarView.topAnchor.constraint(equalTo: topAnchor),
            toolbarView.leadingAnchor.constraint(equalTo: leadingAnchor),
            toolbarView.trailingAnchor.constraint(equalTo: trailingAnchor),
            toolbarView.heightAnchor.constraint(equalToConstant: 50),
            
            toolbarStack.centerYAnchor.constraint(equalTo: toolbarView.centerYAnchor),
            toolbarStack.leadingAnchor.constraint(equalTo: toolbarView.leadingAnchor, constant: 8),
            toolbarStack.trailingAnchor.constraint(equalTo: toolbarView.trailingAnchor, constant: -8),
            
            balanceView.heightAnchor.constraint(equalToConstant: 36),
            balanceView.widthAnchor.constraint(greaterThanOrEqualToConstant: 100),
            
            walletIcon.leadingAnchor.constraint(equalTo: balanceView.leadingAnchor, constant: 10),
            walletIcon.centerYAnchor.constraint(equalTo: balanceView.centerYAnchor),
            walletIcon.widthAnchor.constraint(equalToConstant: 18),
            walletIcon.heightAnchor.constraint(equalToConstant: 18),
            
            balanceLabel.leadingAnchor.constraint(equalTo: walletIcon.trailingAnchor, constant: 6),
            balanceLabel.trailingAnchor.constraint(equalTo: balanceView.trailingAnchor, constant: -10),
            balanceLabel.centerYAnchor.constraint(equalTo: balanceView.centerYAnchor)
        ])
    }
    
    private func setupAuthPrompt() {
        addSubview(authPromptView)
        
        let promptLabel = UILabel()
        promptLabel.text = "Login to use HealthPay payments"
        promptLabel.font = .systemFont(ofSize: 14)
        promptLabel.textColor = UIColor(named: "TextPrimary") ?? .label
        promptLabel.translatesAutoresizingMaskIntoConstraints = false
        
        let loginButton = UIButton(type: .system)
        loginButton.setTitle("Login", for: .normal)
        loginButton.titleLabel?.font = .systemFont(ofSize: 14, weight: .semibold)
        loginButton.addTarget(self, action: #selector(loginTapped), for: .touchUpInside)
        loginButton.translatesAutoresizingMaskIntoConstraints = false
        
        authPromptView.addSubview(promptLabel)
        authPromptView.addSubview(loginButton)
        
        NSLayoutConstraint.activate([
            authPromptView.topAnchor.constraint(equalTo: toolbarView.bottomAnchor),
            authPromptView.leadingAnchor.constraint(equalTo: leadingAnchor),
            authPromptView.trailingAnchor.constraint(equalTo: trailingAnchor),
            authPromptView.heightAnchor.constraint(equalToConstant: 40),
            
            promptLabel.leadingAnchor.constraint(equalTo: authPromptView.leadingAnchor, constant: 16),
            promptLabel.centerYAnchor.constraint(equalTo: authPromptView.centerYAnchor),
            
            loginButton.trailingAnchor.constraint(equalTo: authPromptView.trailingAnchor, constant: -16),
            loginButton.centerYAnchor.constraint(equalTo: authPromptView.centerYAnchor)
        ])
    }
    
    private func setupKeyboard() {
        addSubview(keyboardContainer)
        
        NSLayoutConstraint.activate([
            keyboardContainer.topAnchor.constraint(equalTo: authPromptView.bottomAnchor, constant: 8),
            keyboardContainer.leadingAnchor.constraint(equalTo: leadingAnchor, constant: 4),
            keyboardContainer.trailingAnchor.constraint(equalTo: trailingAnchor, constant: -4),
            keyboardContainer.bottomAnchor.constraint(equalTo: bottomAnchor, constant: -4)
        ])
    }
    
    private func buildKeyboard(keys: [[String]]) {
        // Clear existing keys
        keyboardContainer.arrangedSubviews.forEach { $0.removeFromSuperview() }
        
        for row in keys {
            let rowStack = UIStackView()
            rowStack.axis = .horizontal
            rowStack.spacing = 4
            rowStack.distribution = .fillEqually
            
            for key in row {
                let keyButton = createKeyButton(key: key)
                rowStack.addArrangedSubview(keyButton)
            }
            
            keyboardContainer.addArrangedSubview(rowStack)
        }
    }
    
    private func createKeyButton(key: String) -> UIButton {
        let button = UIButton(type: .system)
        button.setTitle(key, for: .normal)
        
        let isSpecialKey = ["⇧", "⌫", "↵", "123", "🌐", "💳", " ", "ABC"].contains(key)
        
        if key == " " {
            button.setTitle("space", for: .normal)
            button.titleLabel?.font = .systemFont(ofSize: 12)
        } else if isSpecialKey {
            button.titleLabel?.font = .systemFont(ofSize: 16, weight: .medium)
        } else {
            button.titleLabel?.font = .systemFont(ofSize: 22)
        }
        
        button.backgroundColor = isSpecialKey 
            ? (UIColor(named: "KeySpecialBackground") ?? .systemGray4)
            : (UIColor(named: "KeyBackground") ?? .white)
        
        button.setTitleColor(
            UIColor(named: isSpecialKey ? "KeySpecialText" : "KeyText") ?? .label,
            for: .normal
        )
        
        button.layer.cornerRadius = 5
        button.layer.shadowColor = UIColor.black.cgColor
        button.layer.shadowOpacity = 0.1
        button.layer.shadowOffset = CGSize(width: 0, height: 1)
        button.layer.shadowRadius = 0.5
        
        button.addTarget(self, action: #selector(keyTapped(_:)), for: .touchUpInside)
        button.addTarget(self, action: #selector(keyTouchDown(_:)), for: .touchDown)
        button.addTarget(self, action: #selector(keyTouchUp(_:)), for: [.touchUpInside, .touchUpOutside, .touchCancel])
        
        return button
    }
    
    private func createToolbarButton(emoji: String, title: String) -> UIButton {
        let button = UIButton(type: .system)
        
        let stack = UIStackView()
        stack.axis = .vertical
        stack.alignment = .center
        stack.spacing = 2
        stack.isUserInteractionEnabled = false
        
        let emojiLabel = UILabel()
        emojiLabel.text = emoji
        emojiLabel.font = .systemFont(ofSize: 20)
        
        let titleLabel = UILabel()
        titleLabel.text = title
        titleLabel.font = .systemFont(ofSize: 10)
        titleLabel.textColor = UIColor(named: "TextSecondary") ?? .secondaryLabel
        
        stack.addArrangedSubview(emojiLabel)
        stack.addArrangedSubview(titleLabel)
        
        button.addSubview(stack)
        stack.translatesAutoresizingMaskIntoConstraints = false
        stack.centerXAnchor.constraint(equalTo: button.centerXAnchor).isActive = true
        stack.centerYAnchor.constraint(equalTo: button.centerYAnchor).isActive = true
        
        button.translatesAutoresizingMaskIntoConstraints = false
        button.widthAnchor.constraint(equalToConstant: 50).isActive = true
        
        return button
    }
    
    // MARK: - Actions
    
    @objc private func keyTapped(_ sender: UIButton) {
        guard let key = sender.title(for: .normal) else { return }
        
        switch key {
        case "⇧":
            toggleShift()
        case "🌐":
            delegate?.keyboardViewDidTapNextKeyboard(self)
        case "123", "ABC":
            // Toggle symbols/letters - not implemented in this version
            break
        case "space":
            delegate?.keyboardView(self, didTapKey: " ")
        default:
            let outputKey = isShiftActive && !isCapsLock ? key.uppercased() : key
            delegate?.keyboardView(self, didTapKey: outputKey)
            
            if isShiftActive && !isCapsLock {
                isShiftActive = false
                updateKeyboardLayout()
            }
        }
    }
    
    @objc private func keyTouchDown(_ sender: UIButton) {
        UIView.animate(withDuration: 0.05) {
            sender.transform = CGAffineTransform(scaleX: 0.95, y: 0.95)
        }
    }
    
    @objc private func keyTouchUp(_ sender: UIButton) {
        UIView.animate(withDuration: 0.05) {
            sender.transform = .identity
        }
    }
    
    @objc private func payTapped() {
        delegate?.keyboardViewDidTapPay(self)
    }
    
    @objc private func requestTapped() {
        delegate?.keyboardViewDidTapRequest(self)
    }
    
    @objc private func balanceTapped() {
        delegate?.keyboardViewDidTapBalance(self)
    }
    
    @objc private func qrTapped() {
        delegate?.keyboardViewDidTapQR(self)
    }
    
    @objc private func settingsTapped() {
        delegate?.keyboardViewDidTapSettings(self)
    }
    
    @objc private func loginTapped() {
        // Open main app for login
        if let url = URL(string: "healthpay://auth") {
            UIApplication.shared.open(url)
        }
    }
    
    // MARK: - Public Methods
    
    func setAuthenticationState(_ authenticated: Bool) {
        isAuthenticated = authenticated
        updateToolbarState()
        authPromptView.isHidden = authenticated
    }
    
    func setPaymentFeaturesEnabled(_ enabled: Bool) {
        paymentFeaturesEnabled = enabled
        updateToolbarState()
    }
    
    func updateBalance(_ balance: WalletBalance?) {
        currentBalance = balance
        if let balance = balance {
            balanceLabel.text = "EGP \(Int(balance.available))"
        } else {
            balanceLabel.text = "Login"
        }
    }
    
    func showBalanceCard(_ balance: WalletBalance) {
        // Show a brief toast with balance info
        let toastView = UIView()
        toastView.backgroundColor = UIColor.black.withAlphaComponent(0.8)
        toastView.layer.cornerRadius = 8
        toastView.translatesAutoresizingMaskIntoConstraints = false
        
        let label = UILabel()
        label.text = "Balance: EGP \(String(format: "%.2f", balance.available))"
        label.textColor = .white
        label.font = .systemFont(ofSize: 14, weight: .medium)
        label.translatesAutoresizingMaskIntoConstraints = false
        
        toastView.addSubview(label)
        addSubview(toastView)
        
        NSLayoutConstraint.activate([
            label.centerXAnchor.constraint(equalTo: toastView.centerXAnchor),
            label.centerYAnchor.constraint(equalTo: toastView.centerYAnchor),
            label.leadingAnchor.constraint(equalTo: toastView.leadingAnchor, constant: 16),
            label.trailingAnchor.constraint(equalTo: toastView.trailingAnchor, constant: -16),
            
            toastView.centerXAnchor.constraint(equalTo: centerXAnchor),
            toastView.bottomAnchor.constraint(equalTo: keyboardContainer.topAnchor, constant: -8),
            toastView.heightAnchor.constraint(equalToConstant: 40)
        ])
        
        // Animate and remove
        toastView.alpha = 0
        UIView.animate(withDuration: 0.3, animations: {
            toastView.alpha = 1
        }) { _ in
            UIView.animate(withDuration: 0.3, delay: 2.0, options: [], animations: {
                toastView.alpha = 0
            }) { _ in
                toastView.removeFromSuperview()
            }
        }
    }
    
    func showError(_ message: String) {
        // Show error toast
        print("Error: \(message)")
    }
    
    func showAuthPrompt(onLogin: @escaping () -> Void) {
        authPromptView.isHidden = false
    }
    
    func toggleShift() {
        if !isShiftActive {
            isShiftActive = true
            isCapsLock = false
        } else if isShiftActive && !isCapsLock {
            isCapsLock = true
        } else {
            isShiftActive = false
            isCapsLock = false
        }
        updateKeyboardLayout()
    }
    
    func switchLanguage() {
        isArabic.toggle()
        updateKeyboardLayout()
    }
    
    // MARK: - Private Methods
    
    private func updateToolbarState() {
        let enabled = isAuthenticated && paymentFeaturesEnabled
        payButton.alpha = enabled ? 1.0 : 0.5
        requestButton.alpha = enabled ? 1.0 : 0.5
        qrButton.alpha = enabled ? 1.0 : 0.5
        
        payButton.isEnabled = enabled
        requestButton.isEnabled = enabled
        qrButton.isEnabled = enabled
    }
    
    private func updateKeyboardLayout() {
        let keys: [[String]]
        
        if isArabic {
            keys = arabicKeys
        } else if isShiftActive {
            keys = englishKeysShift
        } else {
            keys = englishKeys
        }
        
        buildKeyboard(keys: keys)
    }
}
