package com.sirius.urovoprintertest

import android.device.PrinterManager
import android.os.Bundle
import android.provider.CalendarContract.Colors
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.sirius.urovoprintertest.ui.theme.UrovoPrinterTestTheme
import  android.device.DeviceManager
import androidx.compose.ui.Alignment

class MainActivity : ComponentActivity() {
    val deviceManager = DeviceManager()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val tsn = deviceManager.tidsn
        Toast.makeText(this, tsn, Toast.LENGTH_LONG).show()
        setContent {
            UrovoPrinterTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(color = Color.Blue),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(onClick = {
                            deviceManager.shutdown(true)
                        }) {
                            Text(text = "Try Print")
                        }
                    }
                }
            }
        }
    }

    private fun tryPrint() {
        val printer = PrinterManager()
        val open = printer.open()
        if (open != 0) {
            Toast.makeText(this, "Printer open failed: $open", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Page-based printing (common in Urovo SDK)
            printer.setupPage(576, 100) // 58mm width usually ~576 dots
            printer.drawText(
                "Hello Urovo i9100",
                20, 20,
                "DEFAULT", 24,
                false, false, 0
            )
            printer.drawText(
                "Flutter plugin test (native first)",
                20, 70,
                "DEFAULT", 18,
                false, false, 0
            )

            val res = printer.printPage(0)
            Toast.makeText(this, "Print result: $res", Toast.LENGTH_LONG).show()
        } catch (e: Throwable) {
            Toast.makeText(this, "Print exception: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            try {
                printer.close()
            } catch (_: Throwable) {
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UrovoPrinterTestTheme {
        Greeting("Android")
    }
}