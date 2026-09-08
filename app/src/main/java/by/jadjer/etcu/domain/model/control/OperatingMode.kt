package by.jadjer.etcu.domain.model.control

enum class OperatingMode(val servoMax: Int?) {
    RAIN(300),
    NORMAL(600),
    SPORT(900),
    CUSTOM(null);

    companion object {
        fun fromServoMax(max: Int): OperatingMode {
            return entries.find { it.servoMax == max } ?: CUSTOM
        }
    }
}
