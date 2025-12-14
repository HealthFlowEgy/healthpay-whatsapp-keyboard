import Foundation
import Security
import LocalAuthentication

// MARK: - Wallet Service

/// Service for interacting with HealthPay wallet API
class WalletService {
    
    static let shared = WalletService()
    
    private let baseURL = "https://portal.beta.healthpay.tech/api"
    private let session: URLSession
    private let tokenManager = TokenManager.shared
    
    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 30
        config.timeoutIntervalForResource = 60
        session = URLSession(configuration: config)
    }
    
    // MARK: - Balance
    
    func getBalance() async throws -> WalletBalance {
        let data = try await request(endpoint: "/wallet/balance", method: "GET")
        return try JSONDecoder().decode(WalletBalance.self, from: data)
    }
    
    // MARK: - Payments
    
    func sendPayment(amount: Double, recipientPhone: String, description: String?) async throws -> Transaction {
        let body: [String: Any] = [
            "amount": amount,
            "recipientPhone": recipientPhone,
            "description": description ?? "Payment via HealthPay Keyboard"
        ]
        
        let data = try await request(endpoint: "/wallet/send", method: "POST", body: body)
        return try JSONDecoder().decode(Transaction.self, from: data)
    }
    
    func requestPayment(amount: Double, description: String?) async throws -> PaymentLink {
        let body: [String: Any] = [
            "amount": amount,
            "description": description ?? "",
            "expiresIn": 24
        ]
        
        let data = try await request(endpoint: "/wallet/request", method: "POST", body: body)
        return try JSONDecoder().decode(PaymentLink.self, from: data)
    }
    
    func generatePaymentLink(amount: Double?, description: String?) async throws -> String {
        var body: [String: Any] = [:]
        if let amount = amount { body["amount"] = amount }
        if let description = description { body["description"] = description }
        
        let data = try await request(endpoint: "/wallet/request", method: "POST", body: body)
        let response = try JSONDecoder().decode(PaymentLink.self, from: data)
        return response.link
    }
    
    // MARK: - Transactions
    
    func getTransactionHistory(page: Int = 1, limit: Int = 20) async throws -> [Transaction] {
        let data = try await request(
            endpoint: "/wallet/transactions?page=\(page)&limit=\(limit)",
            method: "GET"
        )
        let response = try JSONDecoder().decode(TransactionListResponse.self, from: data)
        return response.transactions
    }
    
    // MARK: - QR
    
    func generateReceiveQR(amount: Double?, description: String?) async throws -> String {
        var body: [String: Any] = [:]
        if let amount = amount { body["amount"] = amount }
        if let description = description { body["description"] = description }
        
        let data = try await request(endpoint: "/qr/generate", method: "POST", body: body)
        let response = try JSONDecoder().decode(QRCodeResponse.self, from: data)
        return response.qrImage
    }
    
    // MARK: - Network
    
    private func request(endpoint: String, method: String, body: [String: Any]? = nil) async throws -> Data {
        guard let url = URL(string: baseURL + endpoint) else {
            throw WalletError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.setValue("application/json", forHTTPHeaderField: "Accept")
        
        if let token = tokenManager.getAccessToken() {
            request.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization")
        }
        
        if let body = body {
            request.httpBody = try JSONSerialization.data(withJSONObject: body)
        }
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse else {
            throw WalletError.invalidResponse
        }
        
        switch httpResponse.statusCode {
        case 200...299:
            return data
        case 401:
            // Try to refresh token
            if try await refreshToken() {
                return try await self.request(endpoint: endpoint, method: method, body: body)
            }
            throw WalletError.unauthorized
        case 400...499:
            let error = try? JSONDecoder().decode(APIError.self, from: data)
            throw WalletError.apiError(error?.message ?? "Request failed")
        default:
            throw WalletError.serverError
        }
    }
    
    private func refreshToken() async throws -> Bool {
        guard let refreshToken = tokenManager.getRefreshToken() else {
            return false
        }
        
        guard let url = URL(string: baseURL + "/auth/refresh") else {
            return false
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: ["refreshToken": refreshToken])
        
        let (data, response) = try await session.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            tokenManager.clearTokens()
            return false
        }
        
        let authResponse = try JSONDecoder().decode(AuthResponse.self, from: data)
        tokenManager.saveTokens(
            accessToken: authResponse.accessToken,
            refreshToken: authResponse.refreshToken
        )
        
        return true
    }
}

// MARK: - Authentication Manager

class AuthenticationManager {
    
    static let shared = AuthenticationManager()
    
    private let baseURL = "https://portal.beta.healthpay.tech/api"
    private let tokenManager = TokenManager.shared
    
    private init() {}
    
    var isAuthenticated: Bool {
        return tokenManager.getAccessToken() != nil
    }
    
    func login(username: String, password: String) async throws -> Bool {
        guard let url = URL(string: baseURL + "/auth/login") else {
            throw WalletError.invalidURL
        }
        
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: [
            "username": username,
            "password": password
        ])
        
        let (data, response) = try await URLSession.shared.data(for: request)
        
        guard let httpResponse = response as? HTTPURLResponse,
              httpResponse.statusCode == 200 else {
            return false
        }
        
        let authResponse = try JSONDecoder().decode(AuthResponse.self, from: data)
        tokenManager.saveTokens(
            accessToken: authResponse.accessToken,
            refreshToken: authResponse.refreshToken
        )
        
        NotificationCenter.default.post(name: .authStateChanged, object: nil)
        
        return true
    }
    
    func logout() {
        tokenManager.clearTokens()
        NotificationCenter.default.post(name: .authStateChanged, object: nil)
    }
}

// MARK: - Token Manager

class TokenManager {
    
    static let shared = TokenManager()
    
    private let accessTokenKey = "healthpay_access_token"
    private let refreshTokenKey = "healthpay_refresh_token"
    
    private init() {}
    
    func getAccessToken() -> String? {
        return getKeychainValue(key: accessTokenKey)
    }
    
    func getRefreshToken() -> String? {
        return getKeychainValue(key: refreshTokenKey)
    }
    
    func saveTokens(accessToken: String, refreshToken: String) {
        setKeychainValue(value: accessToken, key: accessTokenKey)
        setKeychainValue(value: refreshToken, key: refreshTokenKey)
    }
    
    func clearTokens() {
        deleteKeychainValue(key: accessTokenKey)
        deleteKeychainValue(key: refreshTokenKey)
    }
    
    // MARK: - Keychain Operations
    
    private func setKeychainValue(value: String, key: String) {
        let data = value.data(using: .utf8)!
        
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecAttrAccessGroup as String: "group.tech.healthpay.keyboard",
            kSecValueData as String: data,
            kSecAttrAccessible as String: kSecAttrAccessibleAfterFirstUnlock
        ]
        
        SecItemDelete(query as CFDictionary)
        SecItemAdd(query as CFDictionary, nil)
    }
    
    private func getKeychainValue(key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecAttrAccessGroup as String: "group.tech.healthpay.keyboard",
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        
        var result: AnyObject?
        let status = SecItemCopyMatching(query as CFDictionary, &result)
        
        guard status == errSecSuccess,
              let data = result as? Data,
              let value = String(data: data, encoding: .utf8) else {
            return nil
        }
        
        return value
    }
    
    private func deleteKeychainValue(key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecAttrAccessGroup as String: "group.tech.healthpay.keyboard"
        ]
        
        SecItemDelete(query as CFDictionary)
    }
}

// MARK: - Biometric Helper

class BiometricHelper {
    
    private let context = LAContext()
    
    var isBiometricAvailable: Bool {
        var error: NSError?
        return context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error)
    }
    
    var biometricType: BiometricType {
        guard isBiometricAvailable else { return .none }
        
        switch context.biometryType {
        case .faceID:
            return .faceID
        case .touchID:
            return .touchID
        case .opticID:
            return .opticID
        default:
            return .none
        }
    }
    
    func authenticate(reason: String, completion: @escaping (Bool, Error?) -> Void) {
        guard isBiometricAvailable else {
            completion(false, BiometricError.notAvailable)
            return
        }
        
        context.evaluatePolicy(
            .deviceOwnerAuthenticationWithBiometrics,
            localizedReason: reason
        ) { success, error in
            DispatchQueue.main.async {
                completion(success, error)
            }
        }
    }
    
    func authenticateWithPasscode(reason: String, completion: @escaping (Bool, Error?) -> Void) {
        context.evaluatePolicy(
            .deviceOwnerAuthentication,
            localizedReason: reason
        ) { success, error in
            DispatchQueue.main.async {
                completion(success, error)
            }
        }
    }
}

enum BiometricType {
    case none
    case touchID
    case faceID
    case opticID
}

enum BiometricError: Error {
    case notAvailable
    case failed
    case cancelled
}

// MARK: - Errors

enum WalletError: Error, LocalizedError {
    case invalidURL
    case invalidResponse
    case unauthorized
    case apiError(String)
    case serverError
    case networkError
    
    var errorDescription: String? {
        switch self {
        case .invalidURL:
            return "Invalid URL"
        case .invalidResponse:
            return "Invalid response from server"
        case .unauthorized:
            return "Please login again"
        case .apiError(let message):
            return message
        case .serverError:
            return "Server error occurred"
        case .networkError:
            return "Network connection error"
        }
    }
}

struct APIError: Decodable {
    let message: String?
    let code: String?
}
