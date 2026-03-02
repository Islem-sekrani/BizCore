package edu.Connexion3A7.tests;

import edu.Connexion3A7.services.OkHttpSmsService;

/**
 * Simple SMS test utility.
 * Run this to verify your Twilio configuration before testing in the UI.
 * 
 * Usage:
 *   1. Update YOUR_PHONE_NUMBER below with your phone in E.164 format
 *   2. Ensure config.properties has valid Twilio credentials
 *   3. Run: ./mvnw exec:java -Dexec.mainClass="edu.Connexion3A7.tests.TestSMS"
 */
public class TestSMS {
    
    // ⚠️ REPLACE THIS with your phone number in E.164 format
    // Examples: "+21655123456" (Tunisia), "+33612345678" (France), "+15551234567" (USA)
    private static final String YOUR_PHONE_NUMBER = "+21658410216";
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("BizCore SMS Test Utility");
        System.out.println("=".repeat(60));
        
        OkHttpSmsService smsService = OkHttpSmsService.getInstance();
        
        // Step 1: Check configuration
        System.out.println("\n[1/3] Checking Twilio configuration...");
        boolean configured = smsService.isConfigured();
        
        if (!configured) {
            System.err.println("❌ FAILED: Twilio credentials not configured!");
            System.err.println("\nPlease update src/main/resources/config.properties:");
            System.err.println("  twilio.accountSid=AC... (your Account SID)");
            System.err.println("  twilio.authToken=... (your Auth Token)");
            System.err.println("  twilio.fromNumber=+1... (your Twilio phone number)");
            System.err.println("\nGet credentials from: https://console.twilio.com");
            System.exit(1);
        }
        
        System.out.println("✅ Twilio credentials configured");
        
        // Step 2: Validate phone number format
        System.out.println("\n[2/3] Validating phone number format...");
        if (!YOUR_PHONE_NUMBER.startsWith("+")) {
            System.err.println("❌ FAILED: Phone number must start with '+' (E.164 format)");
            System.err.println("   Current: " + YOUR_PHONE_NUMBER);
            System.err.println("   Example: +21655123456");
            System.exit(1);
        }
        
        if (YOUR_PHONE_NUMBER.length() < 10) {
            System.err.println("❌ FAILED: Phone number too short");
            System.err.println("   Must include country code and full number");
            System.exit(1);
        }
        
        System.out.println("✅ Phone number format valid: " + YOUR_PHONE_NUMBER);
        
        // Step 3: Send test SMS
        System.out.println("\n[3/3] Sending test SMS...");
        System.out.println("   To: " + YOUR_PHONE_NUMBER);
        System.out.println("   Message: Test from BizCore");
        
        smsService.sendSmsAsync(
            YOUR_PHONE_NUMBER,
            "BizCore Test SMS\n" +
            "Si vous recevez ce message, votre configuration SMS fonctionne correctement!\n" +
            "Test effectue le " + java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        );
        
        System.out.println("\n📤 SMS request sent to Twilio API");
        System.out.println("⏳ Waiting 10 seconds for delivery...");
        System.out.println("\nCheck your phone for the SMS.");
        System.out.println("If not received, check:");
        System.out.println("  • Twilio console logs: https://console.twilio.com/us1/monitor/logs/sms");
        System.out.println("  • Your phone can receive SMS");
        System.out.println("  • Number is verified (if using trial account)");
        
        // Keep program alive to allow async SMS to complete
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("Test complete. Check console output above for any errors.");
        System.out.println("=".repeat(60));
    }
}
