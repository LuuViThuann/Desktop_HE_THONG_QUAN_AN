package util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Utility class để generate VietQR code cho thanh toán chuyển khoản
 * VietQR Format: https://portal.vietqr.io/
 */
public class VietQRGenerator {
    
    private static String BANK_BIN = "970405";
    private static String ACCOUNT_NUMBER = "0912345678";
    private static String ACCOUNT_NAME = "RESTAURANT NAME";
    
    public static String generateVietQRString(BigDecimal amount, String invoiceNumber) {
        StringBuilder qr = new StringBuilder();
        qr.append("00020126360014com.vietqr0215");
        qr.append(BANK_BIN);
        qr.append(ACCOUNT_NUMBER);
        qr.append("0368");
        qr.append(ACCOUNT_NAME);
        qr.append("0420");
        
        String amountStr = String.format("%012d", amount.longValue());
        qr.append(amountStr);
        
        String ref = "HD" + invoiceNumber;
        String refLen = String.format("%02d", ref.length());
        qr.append("62").append(refLen).append(ref);
        qr.append("6304");
        
        return qr.toString();
    }
    
    public static BufferedImage generateQRImage(String qrContent, int width, int height) 
            throws WriterException, IOException {
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix matrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, width, height);
        return MatrixToImageWriter.toBufferedImage(matrix);
    }
    
    public static BufferedImage createQRCode(BigDecimal amount, String invoiceNumber) 
            throws WriterException, IOException {
        String qrString = generateVietQRString(amount, invoiceNumber);
        return generateQRImage(qrString, 300, 300);
    }
    
    public static void setBankInfo(String bin, String accountNum, String accountName) {
        BANK_BIN = bin;
        ACCOUNT_NUMBER = accountNum;
        ACCOUNT_NAME = accountName;
    }
    
    public static String getBankInfo() {
        return "Ngân hàng: " + BANK_BIN + "\nTài khoản: " + ACCOUNT_NUMBER + 
               "\nChủ tài khoản: " + ACCOUNT_NAME;
    }
}