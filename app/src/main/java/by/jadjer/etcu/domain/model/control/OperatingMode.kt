package by.jadjer.etcu.domain.model.control

import by.jadjer.etcu.R

enum class OperatingMode(val resId: Int, val servoMax: Int?) {
    RAIN(R.string.mode_rain, 300),
    NORMAL(R.string.mode_normal, 600),
    SPORT(R.string.mode_sport, 900),
    CUSTOM(R.string.mode_custom, null);

    companion object {
        fun fromServoMax(max: Int): OperatingMode {
            return entries.find { it.servoMax == max } ?: CUSTOM
        }
    }
}
