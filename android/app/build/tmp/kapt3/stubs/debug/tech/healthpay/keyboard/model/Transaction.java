package tech.healthpay.keyboard.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Transaction Model
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000<\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u000b\n\u0002\u0010$\n\u0002\b\u001e\n\u0002\u0010\b\n\u0002\b\u0015\n\u0002\u0010\u000b\n\u0002\b\u0004\b\u0086\b\u0018\u00002\u00020\u0001B\u00a9\u0001\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\b\b\u0002\u0010\n\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u0012\u0006\u0010\u0010\u001a\u00020\u0003\u0012\u0006\u0010\u0011\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0013\u001a\u00020\t\u0012\u0016\b\u0002\u0010\u0014\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0015\u00a2\u0006\u0002\u0010\u0016J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010;\u001a\u00020\u0003H\u00c6\u0003J\t\u0010<\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010=\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u0010>\u001a\u00020\tH\u00c6\u0003J\u0017\u0010?\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0015H\u00c6\u0003J\t\u0010@\u001a\u00020\u0005H\u00c6\u0003J\t\u0010A\u001a\u00020\u0007H\u00c6\u0003J\t\u0010B\u001a\u00020\tH\u00c6\u0003J\t\u0010C\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010D\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010E\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010F\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u0010G\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u00b9\u0001\u0010H\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\u00032\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\r\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000e\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u000f\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0010\u001a\u00020\u00032\b\b\u0002\u0010\u0011\u001a\u00020\u00032\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0013\u001a\u00020\t2\u0016\b\u0002\u0010\u0014\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0015H\u00c6\u0001J\u0013\u0010I\u001a\u00020J2\b\u0010K\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010L\u001a\u000204H\u00d6\u0001J\t\u0010M\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\u0018R\u0013\u0010\u0012\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u001aR\u0011\u0010\u001b\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u001c\u0010\u001aR\u0011\u0010\u001d\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b\u001e\u0010\u001aR\u0011\u0010\u0011\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001aR\u0011\u0010\n\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001aR\u0013\u0010\u000f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001aR\u0011\u0010\u0013\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\u0018R\u0011\u0010#\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b$\u0010\u001aR\u0011\u0010%\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b&\u0010\u001aR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001aR\u001f\u0010\u0014\u001a\u0010\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u0001\u0018\u00010\u0015\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0013\u0010\f\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u001aR\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001aR\u0011\u0010\u0010\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u001aR\u0013\u0010\u000e\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b-\u0010\u001aR\u0013\u0010\r\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u001aR\u0011\u0010/\u001a\u00020\u00038F\u00a2\u0006\u0006\u001a\u0004\b0\u0010\u001aR\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b1\u00102R\u0011\u00103\u001a\u0002048F\u00a2\u0006\u0006\u001a\u0004\b5\u00106R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b7\u00108\u00a8\u0006N"}, d2 = {"Ltech/healthpay/keyboard/model/Transaction;", "", "id", "", "type", "Ltech/healthpay/keyboard/model/TransactionType;", "status", "Ltech/healthpay/keyboard/model/TransactionStatus;", "amount", "", "currency", "recipientPhone", "recipientName", "senderPhone", "senderName", "description", "referenceNumber", "createdAt", "completedAt", "fee", "metadata", "", "(Ljava/lang/String;Ltech/healthpay/keyboard/model/TransactionType;Ltech/healthpay/keyboard/model/TransactionStatus;DLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;DLjava/util/Map;)V", "getAmount", "()D", "getCompletedAt", "()Ljava/lang/String;", "counterpartyInitial", "getCounterpartyInitial", "counterpartyName", "getCounterpartyName", "getCreatedAt", "getCurrency", "getDescription", "getFee", "formattedAmount", "getFormattedAmount", "formattedDate", "getFormattedDate", "getId", "getMetadata", "()Ljava/util/Map;", "getRecipientName", "getRecipientPhone", "getReferenceNumber", "getSenderName", "getSenderPhone", "shortDate", "getShortDate", "getStatus", "()Ltech/healthpay/keyboard/model/TransactionStatus;", "statusColor", "", "getStatusColor", "()I", "getType", "()Ltech/healthpay/keyboard/model/TransactionType;", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "", "other", "hashCode", "toString", "app_debug"})
public final class Transaction {
    @org.jetbrains.annotations.NotNull
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.model.TransactionType type = null;
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.model.TransactionStatus status = null;
    private final double amount = 0.0;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String currency = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String recipientPhone = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String recipientName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String senderPhone = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String senderName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String description = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String referenceNumber = null;
    @org.jetbrains.annotations.NotNull
    private final java.lang.String createdAt = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String completedAt = null;
    private final double fee = 0.0;
    @org.jetbrains.annotations.Nullable
    private final java.util.Map<java.lang.String, java.lang.Object> metadata = null;
    
    public Transaction(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.TransactionType type, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.TransactionStatus status, double amount, @org.jetbrains.annotations.NotNull
    java.lang.String currency, @org.jetbrains.annotations.Nullable
    java.lang.String recipientPhone, @org.jetbrains.annotations.Nullable
    java.lang.String recipientName, @org.jetbrains.annotations.Nullable
    java.lang.String senderPhone, @org.jetbrains.annotations.Nullable
    java.lang.String senderName, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.NotNull
    java.lang.String referenceNumber, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String completedAt, double fee, @org.jetbrains.annotations.Nullable
    java.util.Map<java.lang.String, ? extends java.lang.Object> metadata) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.TransactionType getType() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.TransactionStatus getStatus() {
        return null;
    }
    
    public final double getAmount() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCurrency() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRecipientPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getRecipientName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getSenderPhone() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getSenderName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getReferenceNumber() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCreatedAt() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getCompletedAt() {
        return null;
    }
    
    public final double getFee() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.Map<java.lang.String, java.lang.Object> getMetadata() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedAmount() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getFormattedDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getShortDate() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCounterpartyName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String getCounterpartyInitial() {
        return null;
    }
    
    public final int getStatusColor() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component13() {
        return null;
    }
    
    public final double component14() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.util.Map<java.lang.String, java.lang.Object> component15() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.TransactionType component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.TransactionStatus component3() {
        return null;
    }
    
    public final double component4() {
        return 0.0;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.Transaction copy(@org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.TransactionType type, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.TransactionStatus status, double amount, @org.jetbrains.annotations.NotNull
    java.lang.String currency, @org.jetbrains.annotations.Nullable
    java.lang.String recipientPhone, @org.jetbrains.annotations.Nullable
    java.lang.String recipientName, @org.jetbrains.annotations.Nullable
    java.lang.String senderPhone, @org.jetbrains.annotations.Nullable
    java.lang.String senderName, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.NotNull
    java.lang.String referenceNumber, @org.jetbrains.annotations.NotNull
    java.lang.String createdAt, @org.jetbrains.annotations.Nullable
    java.lang.String completedAt, double fee, @org.jetbrains.annotations.Nullable
    java.util.Map<java.lang.String, ? extends java.lang.Object> metadata) {
        return null;
    }
    
    @java.lang.Override
    public boolean equals(@org.jetbrains.annotations.Nullable
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public java.lang.String toString() {
        return null;
    }
}