package com.akira.island.service

import com.akira.island.core.tr

/** Only ride-status copy can claim a non-ongoing taxi notification. */
internal object RideNotification {
    private val duration = Regex("""(?<!\p{L})(\d{1,3})\s*(?:мин(?:ут[аыу]?)?|хв(?:илин[аиу]?)?|min(?:ute)?s?)(?!\p{L})""", RegexOption.IGNORE_CASE)
    private val pickup = Regex(
        """подач|приед|подъез|в пути|прибут|приїд|під.?їждж|\b(?:arriv\w*|pickup|pick.up|on the way|away|approaching|a caminho|chega\w*)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val arrived = Regex(
        """на месте|машина (?:уже )?подана|водитель (?:уже )?жд[её]т|на місці|водій чекає|\b(?:has arrived|driver is here|driver is waiting|motorista chegou)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val matching = Regex(
        """(?:ищем|подбираем) (?:машину|водителя)|шукаємо (?:авто|водія)|\b(?:finding|looking for) (?:a |your )?driver\b""",
        RegexOption.IGNORE_CASE,
    )
    private val enRoute = Regex(
        """(?:водитель|машина|автомобиль) (?:уже )?в пути|водій (?:уже )?в дорозі|\b(?:your )?(?:driver|car) is on the way\b""",
        RegexOption.IGNORE_CASE,
    )
    private val riding = Regex(
        """поездка началась|поездка в процессе|поїздка почалася|\b(?:ride (?:started|in progress)|on your trip)\b""",
        RegexOption.IGNORE_CASE,
    )
    private val finished = Regex(
        """поездка (?:завершена|отменена)|заказ отмен[её]н|оцени(?:те|ть) поездку|поїздку скасовано|\b(?:ride (?:complete[d]?|cancell?ed)|rate your ride)\b""",
        RegexOption.IGNORE_CASE,
    )

    fun isFinished(fields: List<String>): Boolean = fields.any(finished::containsMatchIn)

    fun isActive(fields: List<String>): Boolean =
        !isFinished(fields) && (rideState(fields) != null || pickupEta(fields) != null)

    fun compactStatus(fields: List<String>): String? {
        if (isFinished(fields)) return null
        val state = rideState(fields)
        val eta = if (state == State.ARRIVED || state == State.RIDING) null else pickupEta(fields)
        return eta?.let { "$it " + tr("мин", "min") } ?: when (state) {
            State.ARRIVED -> tr("Подано", "Here")
            State.MATCHING -> tr("Поиск", "Finding")
            State.EN_ROUTE -> tr("В пути", "En route")
            State.RIDING -> tr("В пути", "In ride")
            null -> null
        }
    }

    private enum class State { ARRIVED, RIDING, MATCHING, EN_ROUTE }

    private fun rideState(fields: List<String>): State? = when {
        fields.any(riding::containsMatchIn) -> State.RIDING
        fields.any(arrived::containsMatchIn) -> State.ARRIVED
        fields.any(enRoute::containsMatchIn) -> State.EN_ROUTE
        fields.any(matching::containsMatchIn) -> State.MATCHING
        else -> null
    }

    private fun pickupEta(fields: List<String>): String? {
        if (fields.none(pickup::containsMatchIn)) return null
        val match = fields.firstNotNullOfOrNull { field ->
            if (pickup.containsMatchIn(field)) duration.find(field) else null
        } ?: fields.firstNotNullOfOrNull { duration.matchEntire(it.trim()) }
        return match?.groupValues?.get(1)
    }
}
