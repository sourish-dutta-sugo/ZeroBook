package com.example.data

data class HsnResult(
    val hsnCode: String,
    val description: String
)

private val HSN_KEYWORD_MAP = mapOf(
    "rice" to "1006",
    "wheat" to "1001",
    "flour" to "1101",
    "sugar" to "1701",
    "salt" to "2501",
    "oil" to "1511",
    "milk" to "0401",
    "butter" to "0405",
    "cheese" to "0406",
    "biscuit" to "1905",
    "bread" to "1905",
    "noodle" to "1902",
    "pasta" to "1902",
    "coffee" to "0901",
    "tea" to "0902",
    "chocolate" to "1806",
    "candy" to "1704",
    "soap" to "3401",
    "shampoo" to "3305",
    "toothpaste" to "3306",
    "detergent" to "3402",
    "perfume" to "3303",
    "cream" to "3304",
    "medicine" to "3004",
    "tablet" to "3004",
    "syrup" to "3004",
    "cotton" to "5201",
    "fabric" to "5208",
    "cloth" to "5208",
    "shirt" to "6205",
    "trouser" to "6203",
    "saree" to "5208",
    "shoe" to "6403",
    "sandal" to "6402",
    "bag" to "4202",
    "box" to "4819",
    "bottle" to "3923",
    "plastic" to "3923",
    "paper" to "4802",
    "notebook" to "4820",
    "pen" to "9608",
    "pencil" to "9609",
    "book" to "4901",
    "mobile" to "8517",
    "phone" to "8517",
    "charger" to "8507",
    "cable" to "8544",
    "laptop" to "8471",
    "computer" to "8471",
    "printer" to "8443",
    "fan" to "8414",
    "bulb" to "8539",
    "wire" to "8544",
    "switch" to "8536",
    "battery" to "8506",
    "motor" to "8501",
    "pump" to "8413",
    "pipe" to "3917",
    "cement" to "2523",
    "brick" to "6901",
    "paint" to "3208",
    "furniture" to "9403",
    "chair" to "9401",
    "table" to "9403",
    "wood" to "4407",
    "steel" to "7208",
    "iron" to "7209",
    "aluminium" to "7606",
    "copper" to "7408",
    "gold" to "7108",
    "silver" to "7106",
    "tyre" to "4011",
    "tube" to "4013",
    "car" to "8703",
    "bicycle" to "8712",
    "petrol" to "2710",
    "diesel" to "2710",
    "fertilizer" to "3102",
    "seed" to "1209",
    "vegetable" to "0709",
    "fruit" to "0811",
    "fish" to "0302",
    "chicken" to "0207",
    "meat" to "0201",
    "egg" to "0407",
    "pizza box" to "4819",
    "paper cup" to "4823",
    "plastic container" to "3923",
    "carry bag" to "3923",
    "glass" to "7013",
    "ceramic" to "6911",
    "rubber" to "4016",
    "foam" to "3921",
    "mattress" to "9404",
    "blanket" to "6301",
    "towel" to "6302",
    "curtain" to "6303"
)

fun searchHsn(keyword: String): List<HsnResult> {
    if (keyword.isBlank()) return emptyList()
    val q = keyword.lowercase().trim()
    return HSN_KEYWORD_MAP.entries
        .filter { (key, _) -> key.contains(q) || q.contains(key) }
        .take(5)
        .map { (key, code) -> HsnResult(hsnCode = code, description = key.replaceFirstChar { it.uppercase() }) }
}

fun suggestHsn(keyword: String, products: List<Product>): HsnResult? {
    val query = keyword.trim()
    if (query.length < 2) return null

    val normalizedQuery = query.lowercase()
    val localProductMatch = products
        .asSequence()
        .filter { it.hsnCode.isNotBlank() }
        .map { product ->
            val normalizedName = product.name.trim().lowercase()
            val score = when {
                normalizedName == normalizedQuery -> 0
                normalizedName.startsWith(normalizedQuery) -> 1
                normalizedQuery in normalizedName -> 2
                else -> 3
            }
            score to product
        }
        .filter { it.first < 3 }
        .sortedBy { it.first }
        .firstOrNull()
        ?.second

    if (localProductMatch != null) {
        return HsnResult(
            hsnCode = localProductMatch.hsnCode,
            description = localProductMatch.name
        )
    }

    return searchHsn(query).firstOrNull()
}

fun filterDecimalInput(input: String): String {
    val cleaned = input.filter { it.isDigit() || it == '.' }
    val dotCount = cleaned.count { it == '.' }
    return if (dotCount > 1) {
        val firstDot = cleaned.indexOf('.')
        cleaned.filterIndexed { index, c ->
            c != '.' || index == firstDot
        }
    } else {
        cleaned
    }
}

fun filterHsnInput(input: String): String = input.filter { it.isDigit() }
