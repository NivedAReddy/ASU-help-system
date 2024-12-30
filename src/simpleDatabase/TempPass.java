package simpleDatabase;

import java.time.LocalDateTime;

public class TempPass {
    private String password;
    private LocalDateTime expiry;

    public TempPass(String password, LocalDateTime expiry) {
        this.password = password;
        this.expiry = expiry;
    }

    // Getters
    public String getPassword() {
        return password;
    }

    public LocalDateTime getExpiry() {
        return expiry;
    }

    // Setters (if needed)
    public void setPassword(String password) {
        this.password = password;
    }

    public void setExpiry(LocalDateTime expiry) {
        this.expiry = expiry;
    }
}
