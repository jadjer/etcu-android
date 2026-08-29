package by.jadjer.etcu.ui.features.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import by.jadjer.etcu.R
import by.jadjer.etcu.domain.model.ConnectionState

@Composable
fun ConnectionState.toDisplayString(param: String = ""): String {
    return when (this) {
        ConnectionState.DISCONNECTED -> stringResource(R.string.ble_state_disconnected)
        ConnectionState.CONNECTING -> stringResource(R.string.ble_state_connecting, param)
        ConnectionState.SCANNING -> stringResource(R.string.ble_state_scanning)
        ConnectionState.CONNECTED_DISCOVERING -> stringResource(R.string.ble_state_connected_discovering)
        ConnectionState.SERVICES_DISCOVERED -> stringResource(R.string.ble_state_services_discovered)
        ConnectionState.OTA_SETUP -> stringResource(R.string.ble_state_ota_setup)
        ConnectionState.READING_INFO -> stringResource(R.string.ble_state_subscribed_reading_info)
        ConnectionState.READING_SETTINGS -> stringResource(R.string.ble_state_reading_settings)
        ConnectionState.READY -> stringResource(R.string.ble_state_ready)
        ConnectionState.BLUETOOTH_OFF -> stringResource(R.string.ble_state_bluetooth_off)
        ConnectionState.INVALID_MAC -> stringResource(R.string.ble_state_invalid_mac)
        ConnectionState.ERROR_PROTOCOL -> stringResource(R.string.ble_state_error_protocol)
        ConnectionState.ERROR_CONNECTION -> stringResource(R.string.ble_error_connection, 0)
        ConnectionState.ERROR_SERVICES -> stringResource(R.string.ble_error_services, 0)
        ConnectionState.ERROR_MTU -> stringResource(R.string.ble_error_mtu, 0)
        ConnectionState.ERROR_DESCRIPTOR_WRITE -> stringResource(R.string.ble_error_descriptor_write, 0)
        ConnectionState.ERROR_INFO_NOT_FOUND -> stringResource(R.string.ble_error_info_not_found)
        ConnectionState.ERROR_READ_CHAR -> stringResource(R.string.ble_error_read_char, param, 0)
        ConnectionState.ERROR_WRITE_CHAR -> stringResource(R.string.ble_error_write_char, param, 0)
    }
}
