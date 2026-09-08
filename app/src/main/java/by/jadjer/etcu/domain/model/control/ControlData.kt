package by.jadjer.etcu.domain.model.control

data class ControlData(
    val servo: PositionRange = PositionRange(),
    val accelerator: PositionRange = PositionRange(),
)

data class PositionRange(
    val min: Int = 0,
    val max: Int = 0,
)
