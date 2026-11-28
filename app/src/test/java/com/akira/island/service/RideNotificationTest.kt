package com.akira.island.service

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RideNotificationTest {
    @Test fun pickupNoticeUsesOnlyDurationInCapsule() {
        val fields = listOf("Через 4 минуты приедет", "Белый седан")
        assertTrue(RideNotification.isActive(fields))
        assertEquals("4", RideNotification.compactStatus(fields)?.substringBefore(' '))
        assertEquals("1", RideNotification.compactStatus(listOf("Driver arrives in 1 minute"))?.substringBefore(' '))
    }

    @Test fun pickupDurationCanBeSeparateFromTheStatus() {
        val fields = listOf("Автомобиль приедет", "4 мин")
        assertTrue(RideNotification.isActive(fields))
        assertEquals("4", RideNotification.compactStatus(fields)?.substringBefore(' '))
        assertFalse(RideNotification.isActive(listOf("Скидка", "4 мин")))
    }

    @Test fun recognizableRideStatesStayActiveWithoutAnEta() {
        assertTrue(RideNotification.isActive(listOf("Ищем водителя")))
        assertTrue(RideNotification.isActive(listOf("Водитель в пути")))
        assertTrue(RideNotification.isActive(listOf("Поездка началась")))
    }

    @Test fun arrivalReplacesOutdatedEta() {
        val fields = listOf("Подача через 4 мин", "Водитель ждёт на месте")
        assertTrue(RideNotification.isActive(fields))
        assertEquals(RideNotification.compactStatus(listOf(fields.last())), RideNotification.compactStatus(fields))
    }

    @Test fun offerAndCompletedRideAreNotActive() {
        assertFalse(RideNotification.isActive(listOf("Скидка на поездки", "Акция 10 мин")))
        val finished = listOf("Поездка завершена", "Через 4 мин приедет")
        assertTrue(RideNotification.isFinished(finished))
        assertFalse(RideNotification.isActive(finished))
        assertNull(RideNotification.compactStatus(finished))
    }
}
