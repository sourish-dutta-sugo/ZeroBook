# SECURITY_AUDIT - ZeroBook

## Current Security Posture
The app is currently in an **Insecure Reference State** regarding data at rest and external communication.

## Identified Risks

### 1. Data at Rest (Critical)
- **Problem:** The Room database (`ZeroBook.db`) is completely unencrypted.
- **Impact:** Any entity with root access or physical device access can read PII (GSTIN, PAN, Bank Details) and business financial history.
- **Target:** Implement SQLCipher for database encryption.

### 2. Cleartext Credentials (Critical)
- **Problem:** `BusinessProfile.smtpPassword` is stored as a plain `String` in the unencrypted database.
- **Impact:** Risk of email account compromise.
- **Target:** Use Android Keystore to encrypt the SMTP password before storing it.

### 3. Backup Security (High)
- **Problem:** `android:allowBackup="true"` is enabled without explicit exclusion of the database in `xml/backup_rules.xml`.
- **Impact:** Database could be extracted via `adb backup`.
- **Target:** Audit and tighten backup rules.

### 4. Export Vulnerability (Medium)
- **Problem:** `ExportStorageManager` writes unencrypted DB/CSV exports to public/shared storage.
- **Impact:** Exported files remain on the device and are readable by other apps.
- **Target:** Migrate to Scoped Storage or SAF-mediated exports.

### 5. Signing Key Fallback (Medium)
- **Problem:** Signing configuration includes a fallback to a local `my-upload-key.jks`.
- **Risk:** If this file was ever committed to Git, the signing key is compromised.
- **Target:** Ensure the keystore is excluded from VCS and rotate keys if necessary.

## Remediation Roadmap
1. Integrate **SQLCipher**.
2. Implement **Android Keystore** for SMTP credential encryption.
3. Harden **Backup/Extraction rules**.
4. Refactor **Export logic** for modern Android storage standards.
