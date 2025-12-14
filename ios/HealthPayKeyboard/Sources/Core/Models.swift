import Foundation

// MARK: - Wallet Balance

struct WalletBalance: Codable {
    let available: Double
    let pending: Double
    let currency: String
    let lastUpdated: String
    
    var total: Double {
        return available + pending
    }
    
    var formattedAvailable: String {
        return "\(currency) \(String(format: "%.2f", available))"
    }
    
    var formattedPending: String {
        return "\(currency) \(String(format: "%.2f", pending))"
    }
    
    var formattedTotal: String {
        return "\(currency) \(String(format: "%.2f", total))"
    }
    
    enum CodingKeys: String, CodingKey {
        case available, pending, currency, lastUpdated
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        available = try container.decode(Double.self, forKey: .available)
        pending = try container.decodeIfPresent(Double.self, forKey: .pending) ?? 0
        currency = try container.decodeIfPresent(String.self, forKey: .currency) ?? "EGP"
        lastUpdated = try container.decodeIfPresent(String.self, forKey: .lastUpdated) ?? ""
    }
}

// MARK: - Transaction

struct Transaction: Codable, Identifiable {
    let id: String
    let type: TransactionType
    let status: TransactionStatus
    let amount: Double
    let currency: String
    let recipientPhone: String?
    let recipientName: String?
    let senderPhone: String?
    let senderName: String?
    let description: String?
    let referenceNumber: String
    let createdAt: String
    let completedAt: String?
    let fee: Double
    
    var formattedAmount: String {
        let sign = type == .received ? "+" : "-"
        return "\(sign)\(currency) \(String(format: "%.2f", amount))"
    }
    
    var formattedDate: String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        guard let date = formatter.date(from: createdAt) else {
            return createdAt
        }
        
        let displayFormatter = DateFormatter()
        displayFormatter.dateStyle = .medium
        displayFormatter.timeStyle = .short
        
        return displayFormatter.string(from: date)
    }
    
    var shortDate: String {
        let formatter = ISO8601DateFormatter()
        formatter.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        
        guard let date = formatter.date(from: createdAt) else {
            return createdAt
        }
        
        let displayFormatter = DateFormatter()
        displayFormatter.dateFormat = "MMM dd"
        
        return displayFormatter.string(from: date)
    }
    
    var counterpartyName: String {
        switch type {
        case .sent:
            return recipientName ?? recipientPhone ?? "Unknown"
        case .received:
            return senderName ?? senderPhone ?? "Unknown"
        default:
            return "System"
        }
    }
    
    var counterpartyInitial: String {
        return String(counterpartyName.prefix(1)).uppercased()
    }
    
    enum CodingKeys: String, CodingKey {
        case id, type, status, amount, currency
        case recipientPhone, recipientName, senderPhone, senderName
        case description, referenceNumber, createdAt, completedAt, fee
    }
    
    init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        id = try container.decode(String.self, forKey: .id)
        type = try container.decode(TransactionType.self, forKey: .type)
        status = try container.decode(TransactionStatus.self, forKey: .status)
        amount = try container.decode(Double.self, forKey: .amount)
        currency = try container.decodeIfPresent(String.self, forKey: .currency) ?? "EGP"
        recipientPhone = try container.decodeIfPresent(String.self, forKey: .recipientPhone)
        recipientName = try container.decodeIfPresent(String.self, forKey: .recipientName)
        senderPhone = try container.decodeIfPresent(String.self, forKey: .senderPhone)
        senderName = try container.decodeIfPresent(String.self, forKey: .senderName)
        description = try container.decodeIfPresent(String.self, forKey: .description)
        referenceNumber = try container.decode(String.self, forKey: .referenceNumber)
        createdAt = try container.decode(String.self, forKey: .createdAt)
        completedAt = try container.decodeIfPresent(String.self, forKey: .completedAt)
        fee = try container.decodeIfPresent(Double.self, forKey: .fee) ?? 0
    }
}

enum TransactionType: String, Codable {
    case sent = "SENT"
    case received = "RECEIVED"
    case topup = "TOPUP"
    case withdrawal = "WITHDRAWAL"
    case refund = "REFUND"
    case fee = "FEE"
}

enum TransactionStatus: String, Codable {
    case pending = "PENDING"
    case completed = "COMPLETED"
    case failed = "FAILED"
    case cancelled = "CANCELLED"
}

// MARK: - Transaction List Response

struct TransactionListResponse: Codable {
    let transactions: [Transaction]
    let total: Int
    let page: Int
    let hasMore: Bool
}

// MARK: - Payment Link

struct PaymentLink: Codable {
    let link: String
    let qrCode: String?
    let expiresAt: String
}

// MARK: - QR Code Response

struct QRCodeResponse: Codable {
    let qrData: String
    let qrImage: String
    let expiresAt: String
}

// MARK: - Auth Response

struct AuthResponse: Codable {
    let accessToken: String
    let refreshToken: String
    let expiresIn: Int
    let user: UserInfo?
}

struct UserInfo: Codable {
    let id: String
    let name: String
    let phone: String
    let email: String?
}

// MARK: - Payment Result

struct PaymentResult {
    let isSuccess: Bool
    let transaction: Transaction?
    let errorMessage: String?
    let errorCode: String?
    
    static func success(_ transaction: Transaction) -> PaymentResult {
        return PaymentResult(
            isSuccess: true,
            transaction: transaction,
            errorMessage: nil,
            errorCode: nil
        )
    }
    
    static func error(_ message: String, code: String? = nil) -> PaymentResult {
        return PaymentResult(
            isSuccess: false,
            transaction: nil,
            errorMessage: message,
            errorCode: code
        )
    }
}

// MARK: - Keyboard Settings

struct KeyboardSettings: Codable {
    var hapticFeedback: Bool = true
    var soundFeedback: Bool = false
    var autoCapitalize: Bool = true
    var showPaymentButton: Bool = true
    var defaultLanguage: String = "en"
    var quickPayEnabled: Bool = true
    var biometricRequired: Bool = true
    var sessionTimeout: Int = 15 // minutes
}

// MARK: - Saved Recipient

struct SavedRecipient: Codable, Identifiable {
    let id: String
    let name: String
    let phone: String
    let avatar: String?
    let lastTransactionAt: String?
    let transactionCount: Int
    
    var initial: String {
        return String(name.prefix(1)).uppercased()
    }
}
