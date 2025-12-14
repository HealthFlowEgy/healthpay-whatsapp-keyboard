package tech.healthpay.keyboard.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * QR Code Data
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u001a\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\b\u0018\u00002\u00020\u0001BU\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\u0002\u0010\fJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0019\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003\u00a2\u0006\u0002\u0010\u000eJ\u000b\u0010\u001b\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001c\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001d\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u001e\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J`\u0010\u001f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\u0005H\u00c6\u0001\u00a2\u0006\u0002\u0010 J\u0013\u0010!\u001a\u00020\"2\b\u0010#\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010$\u001a\u00020%H\u00d6\u0001J\t\u0010&\u001a\u00020\u0005H\u00d6\u0001R\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\n\n\u0002\u0010\u000f\u001a\u0004\b\r\u0010\u000eR\u0013\u0010\n\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u0011R\u0013\u0010\u000b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0011R\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0011R\u0013\u0010\t\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0014\u0010\u0011R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0011R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006\'"}, d2 = {"Ltech/healthpay/keyboard/model/QRCodeData;", "", "type", "Ltech/healthpay/keyboard/model/QRType;", "paymentId", "", "amount", "", "recipientPhone", "recipientName", "description", "expiresAt", "(Ltech/healthpay/keyboard/model/QRType;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getAmount", "()Ljava/lang/Double;", "Ljava/lang/Double;", "getDescription", "()Ljava/lang/String;", "getExpiresAt", "getPaymentId", "getRecipientName", "getRecipientPhone", "getType", "()Ltech/healthpay/keyboard/model/QRType;", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "(Ltech/healthpay/keyboard/model/QRType;Ljava/lang/String;Ljava/lang/Double;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ltech/healthpay/keyboard/model/QRCodeData;", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
public final class QRCodeData {
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.model.QRType type = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String paymentId = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.Double amount = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String recipientPhone = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String recipientName = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String description = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String expiresAt = null;
    
    public QRCodeData(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.QRType type, @org.jetbrains.annotations.Nullable
    java.lang.String paymentId, @org.jetbrains.annotations.Nullable
    java.lang.Double amount, @org.jetbrains.annotations.Nullable
    java.lang.String recipientPhone, @org.jetbrains.annotations.Nullable
    java.lang.String recipientName, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.Nullable
    java.lang.String expiresAt) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.QRType getType() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getPaymentId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double getAmount() {
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
    public final java.lang.String getDescription() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getExpiresAt() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.QRType component1() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Double component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
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
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.QRCodeData copy(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.QRType type, @org.jetbrains.annotations.Nullable
    java.lang.String paymentId, @org.jetbrains.annotations.Nullable
    java.lang.Double amount, @org.jetbrains.annotations.Nullable
    java.lang.String recipientPhone, @org.jetbrains.annotations.Nullable
    java.lang.String recipientName, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.Nullable
    java.lang.String expiresAt) {
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