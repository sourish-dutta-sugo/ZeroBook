package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import androidx.core.content.ContextCompat
import com.example.data.Utils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

enum class RequiredBusinessField(val label: String) {
    BusinessName("Business Name"),
    Address("Address"),
    PinCode("PIN Code")
}

fun missingRequiredBusinessFields(
    businessName: String,
    address: String,
    pinCode: String
): List<RequiredBusinessField> = buildList {
    if (businessName.isBlank()) add(RequiredBusinessField.BusinessName)
    if (address.isBlank()) add(RequiredBusinessField.Address)
    if (pinCode.isBlank()) add(RequiredBusinessField.PinCode)
}

fun resolveIndianStateInfo(rawValue: String): Pair<String, String>? {
    val normalized = normalizeStateKey(rawValue)
    if (normalized.isBlank()) return null

    val stateMap = linkedMapOf<String, Pair<String, String>>()
    Utils.INDIAN_STATES.forEach { (name, code) ->
        stateMap[normalizeStateKey(name)] = name to code
    }

    val aliases = mapOf(
        "jammu and kashmir" to ("Jammu & Kashmir" to "01"),
        "nct of delhi" to ("Delhi" to "07"),
        "new delhi" to ("Delhi" to "07"),
        "orissa" to ("Odisha" to "21"),
        "pondicherry" to ("Puducherry" to "34"),
        "andaman and nicobar" to ("Andaman & Nicobar Islands" to "35"),
        "andaman nicobar islands" to ("Andaman & Nicobar Islands" to "35"),
        "dadra and nagar haveli" to ("Dadra & Nagar Haveli" to "26"),
        "daman and diu" to ("Daman & Diu" to "25"),
        "dadra and nagar haveli and daman and diu" to ("Dadra & Nagar Haveli" to "26"),
        "andhra pradesh new" to ("Andhra Pradesh (New)" to "37"),
        "uttaranchal" to ("Uttarakhand" to "05"),
        "lucknow" to ("Uttar Pradesh" to "09"),
        "kolkata" to ("West Bengal" to "19"),
        "calcutta" to ("West Bengal" to "19"),
        "mumbai" to ("Maharashtra" to "27"),
        "bombay" to ("Maharashtra" to "27"),
        "bengaluru" to ("Karnataka" to "29"),
        "bangalore" to ("Karnataka" to "29"),
        "chennai" to ("Tamil Nadu" to "33")
    )

    return stateMap[normalized]
        ?: aliases[normalized]
        ?: stateMap.entries.firstOrNull { normalized.contains(it.key) || it.key.contains(normalized) }?.value
}

private fun normalizeStateKey(value: String): String = value
    .trim()
    .lowercase(Locale.ENGLISH)
    .replace("&", " and ")
    .replace(Regex("[^a-z0-9 ]"), " ")
    .replace(Regex("\\s+"), " ")

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private suspend fun getLastKnownLocation(context: Context): Location? {
    if (!hasLocationPermission(context)) return null
    return suspendCancellableCoroutine { continuation ->
        runCatching {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener { continuation.resume(it) }
                .addOnFailureListener { continuation.resume(null) }
        }.onFailure {
            continuation.resume(null)
        }
    }
}

private suspend fun reverseGeocodeStateInfo(
    context: Context,
    latitude: Double,
    longitude: Double
): Pair<String, String>? = withContext(Dispatchers.IO) {
    if (!Geocoder.isPresent()) return@withContext null
    runCatching {
        @Suppress("DEPRECATION")
        val addresses = Geocoder(context, Locale("en", "IN"))
            .getFromLocation(latitude, longitude, 5)
            .orEmpty()

        addresses.asSequence()
            .flatMap { address ->
                sequenceOf(
                    address.adminArea,
                    address.subAdminArea,
                    address.locality,
                    address.subLocality
                )
            }
            .mapNotNull { candidate -> candidate?.takeIf { it.isNotBlank() } }
            .mapNotNull(::resolveIndianStateInfo)
            .firstOrNull()
    }.getOrNull()
}

suspend fun detectGstStateInfoFromLocation(context: Context): Pair<String, String>? {
    val location = getLastKnownLocation(context) ?: return null
    return reverseGeocodeStateInfo(context, location.latitude, location.longitude)
}
