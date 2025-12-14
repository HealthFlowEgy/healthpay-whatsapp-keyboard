package tech.healthpay.keyboard.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Transaction Types
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\b\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006j\u0002\b\u0007j\u0002\b\b\u00a8\u0006\t"}, d2 = {"Ltech/healthpay/keyboard/model/TransactionType;", "", "(Ljava/lang/String;I)V", "SENT", "RECEIVED", "TOPUP", "WITHDRAWAL", "REFUND", "FEE", "app_debug"})
public enum TransactionType {
    /*public static final*/ SENT /* = new SENT() */,
    /*public static final*/ RECEIVED /* = new RECEIVED() */,
    /*public static final*/ TOPUP /* = new TOPUP() */,
    /*public static final*/ WITHDRAWAL /* = new WITHDRAWAL() */,
    /*public static final*/ REFUND /* = new REFUND() */,
    /*public static final*/ FEE /* = new FEE() */;
    
    TransactionType() {
    }
    
    @org.jetbrains.annotations.NotNull
    public static kotlin.enums.EnumEntries<tech.healthpay.keyboard.model.TransactionType> getEntries() {
        return null;
    }
}