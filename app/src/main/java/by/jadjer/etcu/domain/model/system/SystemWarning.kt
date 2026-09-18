package by.jadjer.etcu.domain.model.system

enum class SystemWarning {
    SPEED_LOW_FOR_CRUISE,
    SPEED_FAST_FOR_CRUISE,
    RPM_LOW_FOR_CRUISE,
    RPM_FAST_FOR_CRUISE,
    CRUISE_NOT_SET,
    SAFETY_ENABLE;

    companion object {
        fun parseWarnings(warningMask: Int): List<SystemWarning> {
            if (warningMask == 0) return emptyList()
            return entries.filter { (warningMask and (1 shl it.ordinal)) != 0 }
        }
    }
}
