package by.jadjer.etcu.domain.model.control

data class ControlData(
    val cruise: CruiseAutoSet = CruiseAutoSet(),
    val servo: PositionRange = PositionRange(),
    val accelerator: PositionRange = PositionRange(),
)

data class CruiseAutoSet(
    val enabled: Boolean = false,
    val delaySec: Int = 0,
    val thresholdKmh: Int = 0,
    val toleranceKmh: Int = 0,
)

data class PositionRange(
    val min: Int = 0,
    val max: Int = 0,
)
