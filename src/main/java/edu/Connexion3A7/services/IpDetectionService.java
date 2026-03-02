package edu.Connexion3A7.services;

import javafx.application.Platform;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * IpDetectionService — detects the user's country from their public IP and
 * provides calling code, digit requirements, and a sample phone number for
 * the phone-prefix UI in AjouterCoach.
 *
 * All network calls run on a daemon Thread; the result callback is always
 * delivered on the JavaFX Application Thread via Platform.runLater().
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * DEV / TEST:
 * Set FORCE_COUNTRY_CODE = "ro" (or any ISO-2) to bypass real IP detection.
 * Set to null for production.
 *
 * VPN TESTING:
 * 1. Connect ProtonVPN to any country (free: Romania, Japan, USA)
 * 2. The polling thread in AjouterCoach detects the change within 10 s
 * 3. No app restart required — flag, prefix and placeholder all update live
 * ──────────────────────────────────────────────────────────────────────────────
 */
public class IpDetectionService {

    // ─── DEV OVERRIDE ─────────────────────────────────────────────────────────
    /** ISO-2 override (e.g. "ro", "us"). Set null for real detection. */
    public static final String FORCE_COUNTRY_CODE = null;

    // ─── Country data record ──────────────────────────────────────────────────
    /**
     * Phone metadata for one country.
     *
     * @param callingCode E.164 prefix including "+" (e.g. "+31")
     * @param digits      Number of local digits required (after prefix)
     * @param example     Representative local number without prefix (e.g.
     *                    "612345678")
     */
    public record CountryData(String callingCode, int digits, String example) {
    }

    // ─── Full detection result ────────────────────────────────────────────────
    /**
     * Object passed to the callback after detection.
     *
     * @param countryCode Uppercase ISO-2 (e.g. "NL"). Never null.
     * @param phone       Phone metadata for the detected country. Never null.
     * @param flagUrl     URL to a 40-px-wide PNG flag image. Never null.
     */
    public record DetectionResult(String countryCode, CountryData phone, String flagUrl) {
    }

    // ─── Comprehensive country map (uppercase ISO-2 → phone metadata) ─────────
    private static final Map<String, CountryData> COUNTRIES = new HashMap<>();

    static {
        // ── Africa ────────────────────────────────────────────────────────────
        COUNTRIES.put("TN", new CountryData("+216", 8, "12345678"));
        COUNTRIES.put("MA", new CountryData("+212", 9, "612345678"));
        COUNTRIES.put("DZ", new CountryData("+213", 9, "551234567"));
        COUNTRIES.put("LY", new CountryData("+218", 9, "912345678"));
        COUNTRIES.put("EG", new CountryData("+20", 10, "1012345678"));
        COUNTRIES.put("NG", new CountryData("+234", 10, "8012345678"));
        COUNTRIES.put("ZA", new CountryData("+27", 9, "712345678"));
        COUNTRIES.put("KE", new CountryData("+254", 9, "712345678"));
        COUNTRIES.put("GH", new CountryData("+233", 9, "241234567"));
        COUNTRIES.put("SN", new CountryData("+221", 9, "701234567"));
        COUNTRIES.put("CI", new CountryData("+225", 10, "0712345678"));
        COUNTRIES.put("CM", new CountryData("+237", 9, "612345678"));
        COUNTRIES.put("ET", new CountryData("+251", 9, "912345678"));
        COUNTRIES.put("TZ", new CountryData("+255", 9, "712345678"));
        COUNTRIES.put("UG", new CountryData("+256", 9, "712345678"));
        COUNTRIES.put("MG", new CountryData("+261", 9, "321234567"));
        COUNTRIES.put("MZ", new CountryData("+258", 9, "821234567"));
        COUNTRIES.put("AO", new CountryData("+244", 9, "912345678"));
        COUNTRIES.put("SD", new CountryData("+249", 9, "912345678"));
        COUNTRIES.put("LR", new CountryData("+231", 8, "77123456"));

        // ── Europe ────────────────────────────────────────────────────────────
        COUNTRIES.put("FR", new CountryData("+33", 9, "612345678"));
        COUNTRIES.put("NL", new CountryData("+31", 9, "612345678"));
        COUNTRIES.put("DE", new CountryData("+49", 11, "15123456789"));
        COUNTRIES.put("GB", new CountryData("+44", 10, "7911123456"));
        COUNTRIES.put("IT", new CountryData("+39", 10, "3121234567"));
        COUNTRIES.put("ES", new CountryData("+34", 9, "612345678"));
        COUNTRIES.put("PT", new CountryData("+351", 9, "912345678"));
        COUNTRIES.put("BE", new CountryData("+32", 9, "470123456"));
        COUNTRIES.put("CH", new CountryData("+41", 9, "791234567"));
        COUNTRIES.put("AT", new CountryData("+43", 10, "6641234567"));
        COUNTRIES.put("SE", new CountryData("+46", 9, "701234567"));
        COUNTRIES.put("NO", new CountryData("+47", 8, "41234567"));
        COUNTRIES.put("DK", new CountryData("+45", 8, "20123456"));
        COUNTRIES.put("FI", new CountryData("+358", 9, "412345678"));
        COUNTRIES.put("PL", new CountryData("+48", 9, "512345678"));
        COUNTRIES.put("RO", new CountryData("+40", 9, "712345678"));
        COUNTRIES.put("HU", new CountryData("+36", 9, "301234567"));
        COUNTRIES.put("CZ", new CountryData("+420", 9, "601234567"));
        COUNTRIES.put("GR", new CountryData("+30", 10, "6912345678"));
        COUNTRIES.put("TR", new CountryData("+90", 10, "5321234567"));
        COUNTRIES.put("RU", new CountryData("+7", 10, "9161234567"));
        COUNTRIES.put("UA", new CountryData("+380", 9, "671234567"));
        COUNTRIES.put("LU", new CountryData("+352", 9, "621234567"));
        COUNTRIES.put("IE", new CountryData("+353", 9, "851234567"));
        COUNTRIES.put("SK", new CountryData("+421", 9, "901234567"));
        COUNTRIES.put("HR", new CountryData("+385", 9, "912345678"));
        COUNTRIES.put("SI", new CountryData("+386", 8, "31234567"));
        COUNTRIES.put("BG", new CountryData("+359", 9, "881234567"));
        COUNTRIES.put("RS", new CountryData("+381", 9, "601234567"));
        COUNTRIES.put("LT", new CountryData("+370", 8, "61234567"));
        COUNTRIES.put("LV", new CountryData("+371", 8, "21234567"));
        COUNTRIES.put("EE", new CountryData("+372", 8, "51234567"));
        COUNTRIES.put("BY", new CountryData("+375", 9, "291234567"));
        COUNTRIES.put("MD", new CountryData("+373", 8, "60123456"));

        // ── Americas ──────────────────────────────────────────────────────────
        COUNTRIES.put("US", new CountryData("+1", 10, "2125551234"));
        COUNTRIES.put("CA", new CountryData("+1", 10, "4161234567"));
        COUNTRIES.put("MX", new CountryData("+52", 10, "5512345678"));
        COUNTRIES.put("BR", new CountryData("+55", 11, "11912345678"));
        COUNTRIES.put("AR", new CountryData("+54", 10, "1112345678"));
        COUNTRIES.put("CO", new CountryData("+57", 10, "3121234567"));
        COUNTRIES.put("CL", new CountryData("+56", 9, "912345678"));
        COUNTRIES.put("PE", new CountryData("+51", 9, "912345678"));
        COUNTRIES.put("VE", new CountryData("+58", 10, "4121234567"));
        COUNTRIES.put("EC", new CountryData("+593", 9, "991234567"));
        COUNTRIES.put("BO", new CountryData("+591", 8, "71234567"));
        COUNTRIES.put("PY", new CountryData("+595", 9, "981234567"));
        COUNTRIES.put("UY", new CountryData("+598", 8, "91234567"));
        COUNTRIES.put("GT", new CountryData("+502", 8, "51234567"));
        COUNTRIES.put("CU", new CountryData("+53", 8, "51234567"));
        COUNTRIES.put("DO", new CountryData("+1", 10, "8091234567"));
        COUNTRIES.put("JM", new CountryData("+1", 10, "8761234567"));

        // ── Middle East & Asia ───────────────────────────────────────────────
        COUNTRIES.put("JP", new CountryData("+81", 10, "9012345678"));
        COUNTRIES.put("CN", new CountryData("+86", 11, "13123456789"));
        COUNTRIES.put("IN", new CountryData("+91", 10, "9123456789"));
        COUNTRIES.put("KR", new CountryData("+82", 10, "1012345678"));
        COUNTRIES.put("SA", new CountryData("+966", 9, "512345678"));
        COUNTRIES.put("AE", new CountryData("+971", 9, "501234567"));
        COUNTRIES.put("QA", new CountryData("+974", 8, "33123456"));
        COUNTRIES.put("KW", new CountryData("+965", 8, "51234567"));
        COUNTRIES.put("LB", new CountryData("+961", 8, "71123456"));
        COUNTRIES.put("JO", new CountryData("+962", 9, "791234567"));
        COUNTRIES.put("IQ", new CountryData("+964", 10, "7901234567"));
        COUNTRIES.put("IR", new CountryData("+98", 10, "9121234567"));
        COUNTRIES.put("PK", new CountryData("+92", 10, "3012345678"));
        COUNTRIES.put("BD", new CountryData("+880", 10, "1712345678"));
        COUNTRIES.put("ID", new CountryData("+62", 11, "81234567890"));
        COUNTRIES.put("MY", new CountryData("+60", 9, "123456789"));
        COUNTRIES.put("SG", new CountryData("+65", 8, "81234567"));
        COUNTRIES.put("TH", new CountryData("+66", 9, "812345678"));
        COUNTRIES.put("VN", new CountryData("+84", 9, "912345678"));
        COUNTRIES.put("PH", new CountryData("+63", 10, "9171234567"));
        COUNTRIES.put("IL", new CountryData("+972", 9, "501234567"));
        COUNTRIES.put("KZ", new CountryData("+7", 10, "7011234567"));
        COUNTRIES.put("UZ", new CountryData("+998", 9, "901234567"));
        COUNTRIES.put("MN", new CountryData("+976", 8, "88123456"));
        COUNTRIES.put("NP", new CountryData("+977", 10, "9841234567"));
        COUNTRIES.put("LK", new CountryData("+94", 9, "771234567"));
        COUNTRIES.put("MM", new CountryData("+95", 9, "912345678"));
        COUNTRIES.put("KH", new CountryData("+855", 9, "912345678"));
        COUNTRIES.put("HK", new CountryData("+852", 8, "51234567"));
        COUNTRIES.put("TW", new CountryData("+886", 9, "912345678"));
        COUNTRIES.put("BH", new CountryData("+973", 8, "36123456"));
        COUNTRIES.put("OM", new CountryData("+968", 8, "92123456"));
        COUNTRIES.put("YE", new CountryData("+967", 9, "712345678"));
        COUNTRIES.put("AF", new CountryData("+93", 9, "701234567"));

        // ── Oceania ───────────────────────────────────────────────────────────
        COUNTRIES.put("AU", new CountryData("+61", 9, "412345678"));
        COUNTRIES.put("NZ", new CountryData("+64", 9, "211234567"));
        COUNTRIES.put("FJ", new CountryData("+679", 7, "7012345"));
        COUNTRIES.put("PG", new CountryData("+675", 8, "71234567"));
    }

    /** Fallback used when the country cannot be determined. */
    private static final DetectionResult FALLBACK = new DetectionResult("TN",
            COUNTRIES.get("TN"),
            flagUrl("tn"));

    // ─── HTTP client ──────────────────────────────────────────────────────────
    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    // ─── Singleton ────────────────────────────────────────────────────────────
    private static final IpDetectionService INSTANCE = new IpDetectionService();

    private IpDetectionService() {
    }

    public static IpDetectionService getInstance() {
        return INSTANCE;
    }

    // ─── Public helpers ───────────────────────────────────────────────────────

    /**
     * Returns phone metadata for the given uppercase ISO-2 country code,
     * or the Tunisia fallback if the code is unknown.
     */
    public static CountryData getCountryData(String countryCode) {
        if (countryCode == null)
            return COUNTRIES.get("TN");
        CountryData data = COUNTRIES.get(countryCode.toUpperCase());
        return data != null ? data : COUNTRIES.get("TN");
    }

    /** Returns the flagcdn.com URL for the given ISO-2 country code. */
    public static String flagUrl(String countryCode) {
        return "https://flagcdn.com/w40/" + countryCode.toLowerCase() + ".png";
    }

    // ─── Async detection (callback on FX thread) ──────────────────────────────

    /**
     * Detects the country asynchronously and calls {@code callback} on the
     * JavaFX Application Thread. Never throws — uses Tunisia fallback on error.
     */
    public void detect(Consumer<DetectionResult> callback) {
        Thread t = new Thread(() -> {
            DetectionResult result = detectInternal();
            Platform.runLater(() -> callback.accept(result));
        }, "ip-detection-thread");
        t.setDaemon(true);
        t.start();
    }

    /**
     * Synchronous detection — blocks the calling thread.
     * Call this ONLY from a non-FX background thread (e.g. inside a scheduler).
     * Returns the detected result, or the Tunisia fallback on any failure.
     */
    public DetectionResult detectSync() {
        return detectInternal();
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private DetectionResult detectInternal() {
        try {
            if (FORCE_COUNTRY_CODE != null) {
                return buildResult(FORCE_COUNTRY_CODE.toUpperCase());
            }
            String ip = fetchPublicIp();
            if (ip == null || ip.isBlank())
                return FALLBACK;

            String cc = fetchCountryCode(ip);
            if (cc == null || cc.isBlank())
                return FALLBACK;

            return buildResult(cc.toUpperCase());
        } catch (Exception e) {
            System.err.println("[IpDetectionService] Detection failed: " + e.getMessage());
            return FALLBACK;
        }
    }

    private DetectionResult buildResult(String upperCc) {
        CountryData data = getCountryData(upperCc);
        return new DetectionResult(upperCc, data, flagUrl(upperCc));
    }

    /** Fetches the machine's public IP from ipify.org. */
    private String fetchPublicIp() {
        try {
            Request req = new Request.Builder()
                    .url("https://api.ipify.org?format=json")
                    .build();
            try (Response resp = HTTP.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null)
                    return null;
                return new JSONObject(resp.body().string()).optString("ip", null);
            }
        } catch (Exception e) {
            System.err.println("[IpDetectionService] ipify error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Queries ip-api.com for the uppercase ISO-2 country code of the given IP.
     * Only requests `countryCode` — the free tier always returns this field.
     */
    private String fetchCountryCode(String ip) {
        try {
            Request req = new Request.Builder()
                    .url("http://ip-api.com/json/" + ip + "?fields=status,countryCode")
                    .build();
            try (Response resp = HTTP.newCall(req).execute()) {
                if (!resp.isSuccessful() || resp.body() == null)
                    return null;
                JSONObject json = new JSONObject(resp.body().string());
                if (!"success".equals(json.optString("status")))
                    return null;
                return json.optString("countryCode", null); // already uppercase from ip-api
            }
        } catch (Exception e) {
            System.err.println("[IpDetectionService] ip-api error: " + e.getMessage());
            return null;
        }
    }

    // ─── Dev self-test ────────────────────────────────────────────────────────

    /** Runs a synchronous self-test and returns a human-readable report. */
    public String testIpDetection() {
        StringBuilder sb = new StringBuilder("─── BizCore IP Detection Test ───\n");
        try {
            String ip = fetchPublicIp();
            if (ip == null)
                throw new Exception("ipify returned null");
            sb.append("✅ IP : ").append(ip).append("\n");

            String cc = fetchCountryCode(ip);
            if (cc == null)
                throw new Exception("ip-api returned null countryCode");
            CountryData data = getCountryData(cc);
            sb.append("✅ Country : ").append(cc)
                    .append("  Prefix: ").append(data.callingCode())
                    .append("  Digits: ").append(data.digits()).append("\n");
            sb.append("✅ Flag URL : ").append(flagUrl(cc)).append("\n");
            sb.append("\n✅ All APIs working");
        } catch (Exception e) {
            sb.append("\n❌ API failed: ").append(e.getMessage());
        }
        String report = sb.toString();
        System.out.println(report);
        return report;
    }
}
