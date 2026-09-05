package by.jadjer.etcu.ui.component.telemetry

import by.jadjer.etcu.domain.model.telemetry.SystemTelemetry

data class SelectedTelemetry(
    val label: String,
    val unit: String,
    val selector: (SystemTelemetry) -> Int
)
