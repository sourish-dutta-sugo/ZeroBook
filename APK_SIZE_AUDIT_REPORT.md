# APK Size Audit Report - ZeroBook

## Executive Summary
The current APK size is approximately **119 MB**. The primary contributors are bundled native libraries and models from ML Kit, and the inclusion of multiple CPU architectures (ABIs). By optimizing these areas, we can target a reduction to the **50–70 MB** range.

---

## 1. Resource Size Analysis

| File | Size | Usage | Recommendation | Expected Reduction |
| :--- | :--- | :--- | :--- | :--- |
| `ic_launcher_symbol.png` | 480 KB | App Icon | Convert to WebP / Crunch PNG | ~200 KB |
| `zerobook_icon.png` | 271 KB | Logo | Convert to WebP / Crunch PNG | ~100 KB |
| Multiple Lang Resources | ~2-5 MB | Library Translations | Use `resConfigs "en"` | ~3 MB |

**Notes:**
* Large image files are not the primary cause of the current size.
* Animations and fonts were checked; no excessive usage found in `res`.

---

## 2. Dependency Size Analysis

| Dependency | Purpose | APK Size Impact | Recommendation | Risk |
| :--- | :--- | :--- | :--- | :--- |
| `mlkit:text-recognition` | OCR | ~25 MB (per ABI) | Switch to `play-services-mlkit-text-recognition` | Requires Play Services on device |
| `mlkit:barcode-scanning` | Scanner | ~10 MB (per ABI) | Switch to `play-services-mlkit-barcode-scanning` | Requires Play Services on device |
| `material-icons-extended` | UI Icons | ~10-15 MB | Use only necessary icons / local drawables | Maintenance effort |
| `androidx.camera` | Camera | ~5 MB | Keep (Required for feature) | None |
| `android-mail` | Email | ~3 MB | Keep (Required for feature) | None |

---

## 3. Code & Build Configuration Analysis

### Current Issues:
* **Missing ABI Filters:** The APK currently includes native libraries for all architectures (armeabi-v7a, arm64-v8a, x86, x86_64).
* **Bundled ML Models:** ML Kit models are bundled inside the APK instead of being downloaded via Play Services.
* **Uncompressed PNGs:** `isCrunchPngs` is set to `false`.
* **Multi-language Bloat:** All available translations from libraries are included.

---

## 4. Proposed Optimization Plan (Phases 2-3)

### Phase 2: Release Build Optimization
1. **Apply ABI Filters:** Limit to `arm64-v8a` and `armeabi-v7a`.
2. **Resource Configurations:** Limit to `en`.
3. **Enable PNG Crunching:** Set `isCrunchPngs = true`.
4. **Optimization Level:** Ensure R8 is at maximum optimization.

### Phase 3: Dependency & Media Optimization
1. **Unbundle ML Kit:** Move to Play Services versions of Text Recognition and Barcode Scanning.
2. **Optimize Icons:** Analyze if `material-icons-extended` can be replaced with a smaller subset or local SVGs.
3. **WebP Conversion:** Convert launcher icons and other PNGs to WebP format.

---

## 5. Estimated Final Size
* **Current:** 119 MB
* **Estimated Reduction:**
  * ABI Filtering: -30 MB
  * ML Kit Unbundling: -20 MB
  * Resource Configs: -3 MB
  * Icon Optimization: -5 MB
* **Estimated Final Size:** **61 MB** (Target: 50-70 MB ✅)
