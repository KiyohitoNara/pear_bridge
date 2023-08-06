/*
 * Copyright 2023 Kiyohito Nara
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.kiyohitonara.pearbridge

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import io.github.kiyohitonara.pearbridge.theme.PearBridgeTheme
import timber.log.Timber

class MainActivity : ComponentActivity() {
    private var bluetoothPeripheral: BluetoothPeripheral? by mutableStateOf(null)
    private val bluetoothPeripheralViewModel: BluetoothPeripheralViewModelImpl by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PearBridgeTheme {
                BluetoothPeripheralsListScreen(peripheral = bluetoothPeripheral, viewModel = bluetoothPeripheralViewModel)
            }
        }

        lifecycle.addObserver(bluetoothPeripheralViewModel)
    }

    override fun onResume() {
        super.onResume()

        requestPermission()
    }

    private fun requestPermission() {
        val permissions = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("ACCESS_COARSE_LOCATION permission is not granted.")

            permissions += android.Manifest.permission.ACCESS_COARSE_LOCATION
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Timber.w("BLUETOOTH_CONNECT permission is not granted.")

                permissions += android.Manifest.permission.BLUETOOTH_CONNECT
            }

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                Timber.w("BLUETOOTH_SCAN permission is not granted.")

                permissions += android.Manifest.permission.BLUETOOTH_SCAN
            }
        }

        if (permissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 0)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothPeripheralsListScreen(peripheral: BluetoothPeripheral?, viewModel: BluetoothPeripheralViewModel) {
    val peripherals by viewModel.peripherals.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.app_name),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.selectableGroup(),
            contentPadding = padding,
        ) {
            items(peripherals) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = it == peripheral,
                            role = Role.RadioButton,
                            onClick = {
                            },
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = it == peripheral,
                        onClick = null,
                    )
                    Text(
                        text = it.name,
                        modifier = Modifier.padding(start = 16.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .selectable(
                            selected = peripheral == null,
                            role = Role.RadioButton,
                            onClick = {
                            },
                        )
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = peripheral == null,
                        onClick = null,
                    )
                    Text(
                        text = stringResource(id = R.string.disconnect),
                        modifier = Modifier.padding(start = 16.dp),
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothPeripheralsListScreenPreview() {
    val viewModel = BluetoothPeripheralViewModelStub(Application())

    PearBridgeTheme {
        BluetoothPeripheralsListScreen(null, viewModel)
    }
}