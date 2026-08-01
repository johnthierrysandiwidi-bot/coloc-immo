package bf.colocation.immo.core

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRENCH).withZone(ZoneId.systemDefault())

private val displayDateTimeFormatter =
    DateTimeFormatter.ofPattern("dd MMM yyyy 'à' HH:mm", Locale.FRENCH).withZone(ZoneId.systemDefault())

/** "2025-06-01T10:00:00Z" -> "01 juin 2025". Renvoie null si non parsable. */
fun String?.formatDate(): String? = this?.let {
    runCatching { displayFormatter.format(Instant.parse(it)) }.getOrNull()
}

fun String?.formatDateTime(): String? = this?.let {
    runCatching { displayDateTimeFormatter.format(Instant.parse(it)) }.getOrNull()
}
