package by.jadjer.etcu.domain.model.control

data class ControlData(
    val autoSet: AutoSet = AutoSet(),
    val servo: Range = Range(),
    val accelerator: Range = Range(),
)

data class AutoSet(
    val enabled: Boolean = false,
    val delay: Int = 0,
    val threshold: Int = 0,
    val tolerance: Int = 0,
)

data class Range(
    val min: Int = 0,
    val max: Int = 0,
)
