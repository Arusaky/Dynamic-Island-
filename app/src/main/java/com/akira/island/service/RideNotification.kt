package com.akira.island.service

import com.akira.island.core.tr

/** Only ride-status copy can claim a non-ongoing taxi notification. */
internal object RideNotification {
    private val duration = Regex("""(?<!\p{L})(\d{1,3})\s*(?:мин(?:ут[аыу]?)?|min(?:ute)?s?)(?!\p{L})""", RegexOption.IGNORE_CASE)
    private val pickup = Regex(
        """подач|приед|подъез|в пути|прибут|приїд|під.?їждж|\b(?:arriv\w*|pickup|pick.up|on the way|away|approaching|a caminho|chega\w*)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val arrived = Regex(
        """на месте|машина (?:уже )?подана|водитель (?:уже )?жд[её]т|на місці|водій чекає|\b(?:has arrived|driver is here|driver is waiting|motorista chegou)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val finished = Regex(
        """поездка (?:завершена|отменена)|заказ отмен[её]н|оцени(?:те|ть) поездку|поїздку скасовано|\b(?:ride (?:complete[d]?|cancell?ed)|rate your ride)\b""",
        RegexOption.IGNORE_CASE,
    )

    fun isFinished(fields: List<String>): Boolean = fields.any(finished::containsMatchIn)

    fun isActive(fields: List<String>): Boolean =
        !isFinished(fields) && fields.any { arrived.containsMatchIn(it) || pickup.containsMatchIn(it) && duration.containsMatchIn(it) }

    fun compactStatus(fields: List<String>): String? {
        if (isFinished(fields)) return null
        if (fields.any(arrived::containsMatchIn)) return tr("Подано", "Here")
        val eta = fields.firstNotNullOfOrNull { field ->
            if (pickup.containsMatchIn(field)) duration.find(field) else duration.matchEntire(field.trim())
        }
        return eta?.let { it.groupValues[1] + " " + tr("мин", "min") }
    }
}
