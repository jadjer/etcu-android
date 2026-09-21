package by.jadjer.etcu.ui.util

import android.content.Context
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ble.ConnectionState
import by.jadjer.etcu.domain.model.system.SystemWarning
import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry
import by.jadjer.etcu.domain.repository.BLERepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.math.roundToInt

class TelemetryNotificationMonitor(
    private val context: Context,
    repository: BLERepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val notificationHelper = NotificationHelper(context)

    // State tracking to avoid spam
    private var lastEngineOverheat = false
    private var lastServoOverheat = false
    private var lastVoltageStatus = 0
    private var lastCruiseActivated = false
    private var lastFailReason: String? = null
    private var lastEcuDisconnected = false
    private var lastServoDisconnected = false

    // Thresholds
    private val engineOverheatThreshold = 106
    private val engineRecoveryThreshold = 95
    private val servoOverheatThreshold = 70
    private val servoRecoveryThreshold = 65
    private val voltageLowThreshold = 11.5f
    private val voltageHighThreshold = 14.8f
    private val voltageRecoveryLow = 12.0f
    private val voltageRecoveryHigh = 14.5f

    init {
        repository.telemetry
            .combine(repository.controlData) { telemetry, control -> telemetry to control }
            .combine(repository.connectionState) { (telemetry, control), state -> Triple(telemetry, control, state) }
            .conflate()
            .onEach { (telemetry, control, state) ->
                checkTelemetry(telemetry, state)
            }
            .launchIn(scope)
    }

    private fun checkTelemetry(telemetry: SystemTelemetry, state: ConnectionState) {
        // Мы проверяем отключение ECU и Servo только если BLE-соединение полностью готово (ConnectionState.READY)
        if (state == ConnectionState.READY) {
            // Обработка ECU
            if (telemetry.ecu.isConnected) {
                if (lastEcuDisconnected) {
                    lastEcuDisconnected = false
                    notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_ECU_DISCONNECT)
                }
                checkEngineTemp(telemetry.ecu.coolantTemp)
                checkVoltage(telemetry.ecu.battery)
                checkCruiseState(telemetry)
            } else {
                clearEcuNotifications()
                if (!lastEcuDisconnected) {
                    notificationHelper.showNotification(
                        NotificationHelper.NOTIF_ID_ECU_DISCONNECT,
                        NotificationHelper.CHANNEL_ALERTS,
                        context.getString(R.string.notif_ecu_disconnected),
                        context.getString(R.string.notif_ecu_disconnected_desc)
                    )
                    lastEcuDisconnected = true
                }
            }

            // Обработка Servo
            if (telemetry.servo.isConnected) {
                if (lastServoDisconnected) {
                    lastServoDisconnected = false
                    notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_SERVO_DISCONNECT)
                }
                checkServoTemp(telemetry.servo.temperature)
            } else {
                clearServoNotifications()
                if (!lastServoDisconnected) {
                    notificationHelper.showNotification(
                        NotificationHelper.NOTIF_ID_SERVO_DISCONNECT,
                        NotificationHelper.CHANNEL_ALERTS,
                        context.getString(R.string.notif_servo_disconnected),
                        context.getString(R.string.notif_servo_disconnected_desc)
                    )
                    lastServoDisconnected = true
                }
            }
        } else {
            // Если само BLE-устройство не подключено или находится в процессе подключения,
            // очищаем все внутренние нотификации отключения компонентов, чтобы не спамить.
            clearEcuNotifications()
            clearServoNotifications()
            if (lastEcuDisconnected) {
                lastEcuDisconnected = false
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_ECU_DISCONNECT)
            }
            if (lastServoDisconnected) {
                lastServoDisconnected = false
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_SERVO_DISCONNECT)
            }
        }
    }

    private fun clearEcuNotifications() {
        if (lastEngineOverheat) {
            lastEngineOverheat = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_ENGINE_TEMP)
        }
        if (lastVoltageStatus != 0) {
            lastVoltageStatus = 0
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_VOLTAGE)
        }
        if (lastCruiseActivated) {
            lastCruiseActivated = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE)
        }
        if (lastFailReason != null) {
            lastFailReason = null
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE_FAIL)
        }
    }

    private fun clearServoNotifications() {
        if (lastServoOverheat) {
            lastServoOverheat = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_SERVO_TEMP)
        }
    }

    private fun checkEngineTemp(temp: Int) {
        if (temp > engineOverheatThreshold && !lastEngineOverheat) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_ENGINE_TEMP,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_engine_overheat),
                context.getString(R.string.notif_engine_overheat_desc, temp)
            )
            lastEngineOverheat = true
        } else if (temp < engineRecoveryThreshold && lastEngineOverheat) {
            lastEngineOverheat = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_ENGINE_TEMP)
        }
    }

    private fun checkServoTemp(temp: Int) {
        if (temp > servoOverheatThreshold && !lastServoOverheat) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_SERVO_TEMP,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_servo_overheat),
                context.getString(R.string.notif_servo_overheat_desc, temp)
            )
            lastServoOverheat = true
        } else if (temp < servoRecoveryThreshold && lastServoOverheat) {
            lastServoOverheat = false
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_SERVO_TEMP)
        }
    }

    private fun checkVoltage(voltage: Float) {
        // Округляем до 1 знака для стабильности уведомления
        val displayVoltage = (voltage * 10).roundToInt() / 10f
        
        if (voltage < voltageLowThreshold && lastVoltageStatus != 1) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_VOLTAGE,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_voltage_low),
                context.getString(R.string.notif_voltage_low_desc, displayVoltage)
            )
            lastVoltageStatus = 1
        } else if (voltage > voltageHighThreshold && lastVoltageStatus != 2) {
            notificationHelper.showNotification(
                NotificationHelper.NOTIF_ID_VOLTAGE,
                NotificationHelper.CHANNEL_ALERTS,
                context.getString(R.string.notif_voltage_high),
                context.getString(R.string.notif_voltage_high_desc, displayVoltage)
            )
            lastVoltageStatus = 2
        } else if (voltage in voltageRecoveryLow..voltageRecoveryHigh && lastVoltageStatus != 0) {
            lastVoltageStatus = 0
            notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_VOLTAGE)
        }
    }

    private fun checkCruiseState(telemetry: SystemTelemetry) {
        val currentActivated = telemetry.cruise.isActivated

        // Notify on On/Off
        if (currentActivated != lastCruiseActivated) {
            if (currentActivated) {
                notificationHelper.showNotification(
                    NotificationHelper.NOTIF_ID_CRUISE,
                    NotificationHelper.CHANNEL_CRUISE,
                    context.getString(R.string.notif_cruise_on),
                    ""
                )
                lastFailReason = null
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE_FAIL)
            } else {
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE)
            }
            lastCruiseActivated = currentActivated
        }

        // Notify on activation failure based strictly on incoming warning channel data
        val activeWarning = telemetry.status.activeWarnings.firstOrNull()
        if (activeWarning != null) {
            val failReason = when (activeWarning) {
                SystemWarning.SPEED_LOW_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_speed_low,
                    telemetry.ecu.speed
                )

                SystemWarning.SPEED_FAST_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_speed_high,
                    telemetry.ecu.speed
                )

                SystemWarning.RPM_LOW_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_rpm_low,
                    telemetry.ecu.rpm
                )

                SystemWarning.RPM_FAST_FOR_CRUISE -> context.getString(
                    R.string.notif_cruise_failed_rpm_high,
                    telemetry.ecu.rpm
                )

                SystemWarning.CRUISE_NOT_SET -> context.getString(R.string.notif_cruise_failed_not_set)
                SystemWarning.SAFETY_ENABLE -> context.getString(R.string.notif_cruise_failed_safety)
            }

            if (failReason != lastFailReason) {
                notificationHelper.showNotification(
                    NotificationHelper.NOTIF_ID_CRUISE_FAIL,
                    NotificationHelper.CHANNEL_CRUISE,
                    context.getString(R.string.notif_cruise_failed),
                    failReason
                )
                lastFailReason = failReason
            }
        } else {
            if (lastFailReason != null) {
                notificationHelper.cancelNotification(NotificationHelper.NOTIF_ID_CRUISE_FAIL)
                lastFailReason = null
            }
        }
    }
}
