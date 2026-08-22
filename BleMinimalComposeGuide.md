# Минималистичное Android-приложение для работы с BLE (Jetpack Compose)

Это готовое руководство и шаблон кода для создания приложения в Android Studio. Оно запрашивает разрешения, подключается к устройству по его MAC-адресу, запрашивает MTU 517 и подписывается на уведомления (Notifications).

## 1. Настройка AndroidManifest.xml

Добавьте следующие разрешения внутри тега `<manifest>`:

```xml
<!-- Разрешения для работы с BLE -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

## 2. Зависимости (build.gradle.kts)

Для работы с Compose Accompanist Permissions (для удобного запроса разрешений):

```kotlin
dependencies {
    implementation("com.google.accompanist:accompanist-permissions:0.34.0")
}
```

## 3. Менеджер BLE (BleManager.kt)

Класс для управления BLE-соединением, запроса MTU и подписки на уведомления.

```kotlin
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothProfile
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

@SuppressLint("MissingPermission")
class BleManager(private val context: Context) {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothGatt: BluetoothGatt? = null

    // UUID вашей службы и характеристики (замените на свои)
    private val SERVICE_UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb") // Пример: Heart Rate
    private val CHARACTERISTIC_UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb") // Пример: Heart Rate Measurement
    private val CLIENT_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

    private val _connectionState = MutableStateFlow("Отключено")
    val connectionState: StateFlow<String> = _connectionState

    private val _receivedData = MutableStateFlow("Нет данных")
    val receivedData: StateFlow<String> = _receivedData

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    _connectionState.value = "Подключено. Запрос MTU..."
                    // ШАГ 1: Запрашиваем MTU 517 после успешного подключения
                    gatt.requestMtu(517)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    _connectionState.value = "Отключено"
                    gatt.close()
                    bluetoothGatt = null
                }
            } else {
                _connectionState.value = "Ошибка подключения: $status"
                gatt.close()
                bluetoothGatt = null
            }
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = "MTU изменен на $mtu. Поиск служб..."
                // ШАГ 2: Обнаруживаем службы после изменения MTU
                gatt.discoverServices()
            } else {
                _connectionState.value = "Не удалось изменить MTU. Поиск служб..."
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                _connectionState.value = "Службы найдены. Подписка..."
                // ШАГ 3: Подписываемся на уведомления характеристики
                subscribeToCharacteristic(gatt)
            } else {
                _connectionState.value = "Ошибка поиска служб: $status"
            }
        }

        // Для Android 13 (API 33) и выше используется новая сигнатура. Ниже универсальный вариант:
        @Deprecated("Deprecated in Java")
        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            if (characteristic.uuid == CHARACTERISTIC_UUID) {
                val value = characteristic.value
                val hexString = value.joinToString(separator = " ") { String.format("%02X", it) }
                _receivedData.value = "Данные (HEX): $hexString"
            }
        }

        // Для новых версий Android (API 33+) раскомментируйте этот метод:
        // override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, value: ByteArray) {
        //     if (characteristic.uuid == CHARACTERISTIC_UUID) {
        //         val hexString = value.joinToString(separator = " ") { String.format("%02X", it) }
        //         _receivedData.value = "Данные (HEX): $hexString"
        //     }
        // }
    }

    fun connect(macAddress: String) {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _connectionState.value = "Bluetooth выключен или недоступен"
            return
        }

        try {
            val device = bluetoothAdapter.getRemoteDevice(macAddress)
            _connectionState.value = "Соединение с $macAddress..."
            // Используем TRANSPORT_LE для BLE-устройств
            bluetoothGatt = device.connectGatt(context, false, gattCallback, BluetoothDevice.TRANSPORT_LE)
        } catch (e: IllegalArgumentException) {
            _connectionState.value = "Неверный MAC-адрес"
        }
    }

    fun disconnect() {
        bluetoothGatt?.disconnect()
    }

    private fun subscribeToCharacteristic(gatt: BluetoothGatt) {
        val service = gatt.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)

        if (characteristic != null) {
            // Включаем уведомления локально в стеке Android
            gatt.setCharacteristicNotification(characteristic, true)

            // Записываем дескриптор на удаленное устройство, чтобы оно начало слать пакеты
            val descriptor = characteristic.getDescriptor(CLIENT_CONFIG_DESCRIPTOR_UUID)
            if (descriptor != null) {
                descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                gatt.writeDescriptor(descriptor)
                _connectionState.value = "Подписка успешна. Ожидание данных..."
            } else {
                _connectionState.value = "Дескриптор не найден"
            }
        } else {
            _connectionState.value = "Характеристика не найдена"
        }
    }
}
```

*Примечание: Если `BluetoothDevice.TRANSPORT_LE` подчеркивается, добавьте класс импорта `android.bluetooth.BluetoothDevice`.*

## 4. Интерфейс (MainActivity.kt)

Основной UI на Jetpack Compose с запросом разрешений рантайма.

```kotlin
import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BleAppScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun BleAppScreen() {
    val context = LocalContext.current
    val bleManager = remember { BleManager(context.applicationContext) }
    
    val connectionState by bleManager.connectionState.collectAsState()
    val receivedData by bleManager.receivedData.collectAsState()
    
    var macAddress by remember { mutableStateOf("00:11:22:33:44:55") } // Замените на MAC вашего BLE-модуля

    // Список необходимых разрешений в зависимости от версии Android
    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionState = rememberMultiplePermissionsState(permissions = blePermissions)

    LaunchedEffect(Unit) {
        permissionState.launchMultiplePermissionRequest()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (permissionState.allPermissionsGranted) {
            OutlinedTextField(
                value = macAddress,
                onValueChange = { macAddress = it },
                label = { Text("MAC Адрес устройства") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = { bleManager.connect(macAddress) }) {
                    Text("Подключить")
                }
                Button(onClick = { bleManager.disconnect() }) {
                    Text("Отключить")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(text = "Статус: $connectionState", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = receivedData, style = MaterialTheme.typography.titleMedium)
        } else {
            Text(text = "Для работы BLE необходимы разрешения")
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { permissionState.launchMultiplePermissionRequest() }) {
                Text("Предоставить разрешения")
            }
        }
    }
}
```

## Важные нюансы реализации:
1. **Последовательность вызовов:** Стек BLE в Android строго последователен. Нельзя вызывать `discoverServices()` одновременно с `requestMtu()`. Код выше соблюдает правильный тайминг: `connectGatt` -> `onConnectionStateChange` -> `requestMtu` -> `onMtuChanged` -> `discoverServices` -> `onServicesDiscovered` -> подписка.
2. **Дескриптор 0x2902:** Для включения `Notification` на стороне сервера (периферийного устройства) обязательно нужно записать значение `ENABLE_NOTIFICATION_VALUE` в CCCD дескриптор характеристики. Без этого устройство не начнет отправку данных.
