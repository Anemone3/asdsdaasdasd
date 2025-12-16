package isil.pe.glassimport.services;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationService {

    private static class Verification {
        final String code;
        final Instant expiresAt;

        Verification(String code, Instant expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }
    }

    // Map<email, Verification>
    private final Map<String, Verification> codes = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateAndStoreCode(String email, int minutesValid) {
        String code = String.format("%06d", random.nextInt(1_000_000)); // 6 dígitos
        Instant expiresAt = Instant.now().plusSeconds(minutesValid * 60L);
        codes.put(email.toLowerCase(), new Verification(code, expiresAt));
        return code;
    }

    public boolean verifyCode(String email, String code) {
        Verification v = codes.get(email.toLowerCase());
        if (v == null) return false;
        if (Instant.now().isAfter(v.expiresAt)) {
            codes.remove(email.toLowerCase());
            return false; // expirado
        }
        boolean ok = v.code.equals(code);
        if (ok) codes.remove(email.toLowerCase()); // consume-once
        return ok;
    }
}