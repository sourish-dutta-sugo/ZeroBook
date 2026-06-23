package com.example.data

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {

    val INDIAN_STATES = listOf(
        "Jammu & Kashmir" to "01",
        "Himachal Pradesh" to "02",
        "Punjab" to "03",
        "Chandigarh" to "04",
        "Uttarakhand" to "05",
        "Haryana" to "06",
        "Delhi" to "07",
        "Rajasthan" to "08",
        "Uttar Pradesh" to "09",
        "Bihar" to "10",
        "Sikkim" to "11",
        "Arunachal Pradesh" to "12",
        "Nagaland" to "13",
        "Manipur" to "14",
        "Mizoram" to "15",
        "Tripura" to "16",
        "Meghalaya" to "17",
        "Assam" to "18",
        "West Bengal" to "19",
        "Jharkhand" to "20",
        "Odisha" to "21",
        "Chhattisgarh" to "22",
        "Madhya Pradesh" to "23",
        "Gujarat" to "24",
        "Daman & Diu" to "25",
        "Dadra & Nagar Haveli" to "26",
        "Maharashtra" to "27",
        "Andhra Pradesh" to "28",
        "Karnataka" to "29",
        "Goa" to "30",
        "Lakshadweep" to "31",
        "Kerala" to "32",
        "Tamil Nadu" to "33",
        "Puducherry" to "34",
        "Andaman & Nicobar Islands" to "35",
        "Telangana" to "36",
        "Andhra Pradesh (New)" to "37",
        "Ladakh" to "38"
    )

    fun formatIndianCurrency(amount: Double): String {
        val isNegative = amount < 0
        val absAmount = Math.abs(amount)
        val parts = DecimalFormat("0.00").format(absAmount).split(".")
        val wholePart = parts[0]
        val decimalPart = parts[1]

        val length = wholePart.length
        val formattedWhole = if (length <= 3) {
            wholePart
        } else {
            val lastThree = wholePart.substring(length - 3)
            val rest = wholePart.substring(0, length - 3)
            val revRest = rest.reversed()
            val chunked = mutableListOf<String>()
            var i = 0
            while (i < revRest.length) {
                val end = Math.min(i + 2, revRest.length)
                chunked.add(revRest.substring(i, end).reversed())
                i += 2
            }
            val restFormatted = chunked.reversed().joinToString(",")
            "$restFormatted,$lastThree"
        }

        return if (isNegative) "-₹$formattedWhole.$decimalPart" else "₹$formattedWhole.$decimalPart"
    }

    fun formatDate(timestamp: Long): String {
        return SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(timestamp))
    }

    fun numberToWords(number: Double): String {
        val words = numberToIndianWords(number)
        return if (words.isBlank()) "Zero Rupees Only" else "$words Rupees Only"
    }

    /** Returns words only, e.g. "Two Thousand Seven Hundred Thirty Nine" or with paise. */
    fun numberToIndianWords(number: Double): String {
        val absolute = kotlin.math.abs(number)
        val rupees = kotlin.math.floor(absolute).toLong()
        val paise = kotlin.math.round((absolute - rupees) * 100.0).toInt().coerceIn(0, 99)

        val units = arrayOf(
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
            "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        )
        val tens = arrayOf(
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        )

        fun convertLessThanOneThousand(n: Int): String {
            if (n == 0) return ""
            var temp = n
            var str = ""
            if (temp >= 100) {
                str += units[temp / 100] + " Hundred "
                temp %= 100
            }
            if (temp >= 20) {
                str += tens[temp / 10] + " "
                temp %= 10
            }
            if (temp > 0) {
                str += units[temp] + " "
            }
            return str.trim()
        }

        fun convertInt(n: Long): String {
            if (n == 0L) return "Zero"
            var tempAmt = n
            var words = ""
            if (tempAmt >= 10000000L) {
                words += convertInt(tempAmt / 10000000L) + " Crore "
                tempAmt %= 10000000
            }
            if (tempAmt >= 100000L) {
                words += convertInt(tempAmt / 100000L) + " Lakh "
                tempAmt %= 100000
            }
            if (tempAmt >= 1000L) {
                words += convertInt(tempAmt / 1000L) + " Thousand "
                tempAmt %= 1000
            }
            if (tempAmt > 0) {
                words += convertLessThanOneThousand(tempAmt.toInt())
            }
            return words.trim()
        }

        val rupeeWords = convertInt(rupees)
        return if (paise > 0) {
            "$rupeeWords and ${convertInt(paise.toLong())} Paise"
        } else {
            rupeeWords
        }
    }

}
