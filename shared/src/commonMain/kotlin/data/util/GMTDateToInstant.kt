package data.util

import io.ktor.util.date.GMTDate
import kotlinx.datetime.Instant

fun GMTDate.toInstant(): Instant {
    return Instant.fromEpochMilliseconds(this.timestamp)
}
