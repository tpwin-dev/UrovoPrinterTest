package com.sirius.urovoprintertest

import android.app.AlertDialog
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
import android.device.Led
import androidx.compose.ui.Alignment
import android.device.LedManager
import android.device.SettingProperty
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.widget.ImageView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.core.graphics.createBitmap
import android.graphics.Color as C
import androidx.core.graphics.get
import androidx.core.graphics.set

class MainActivity : ComponentActivity() {
    val deviceManager = DeviceManager()
    val ledManager = LedManager()
    val printer = PrinterManager()
    val settingProperty = SettingProperty()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        Toast.makeText(applicationContext,printer.status.toString(),Toast.LENGTH_LONG).show()
        setContent {
            UrovoPrinterTestTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(color = Color.White),
                        verticalArrangement = Arrangement.SpaceAround

                    ) {
                        Text("class MainScreen extends ConsumerStatefulWidget {\n" +
                                "  static const routeName = 'MainScreen';\n" +
                                "  const MainScreen._({super.key});\n" +
                                "  static void navigate(BuildContext context) {\n" +
                                "    context.navigateWithRedialRevealAnimation(MainScreen._());\n" +
                                "  }\n" +
                                "\n" +
                                "  @override\n" +
                                "  ConsumerState<ConsumerStatefulWidget> createState() => _MainScreenState();\n" +
                                "}\n")
                        Button(onClick = {
                            try {

                                ledManager.enableLedIndicator(Led.LED_4,true)
                            }catch (e:Exception){
                                Toast.makeText(applicationContext,e.toString(),Toast.LENGTH_LONG).show()
                            }

//                            val ac = BitmapFactory.decodeResource(resources, R.drawable.people_thinking)
////            val ac = captureScreen()
////                            val b1 = toOpaqueWhite(ac)
//                            val b2 = scaleToWidth(ac, 384)     // try 384 first
//                            val b3 = toPureBlackWhite(b2, threshold = 110)
//                            showPrintPreviewDialog(b2)

//                            ledManager.enableLedIndicator(Led.LED_1,true)
                        }) {
                            Text(text = "Try Print")
                        }
                    }
                }
            }
        }
    }

    fun toPureBlackWhite(src: Bitmap, threshold: Int = 180): Bitmap {
        // Step 1: Convert to grayscale
        val grayscale = createBitmap(src.width, src.height)
        val canvas = Canvas(grayscale)

        val paint = Paint()
        val matrix = ColorMatrix()
        matrix.setSaturation(0f)

        paint.colorFilter = ColorMatrixColorFilter(matrix)
        canvas.drawBitmap(src, 0f, 0f, paint)

        // Step 2: Apply threshold
        val pixels = IntArray(src.width * src.height)
        grayscale.getPixels(pixels, 0, src.width, 0, 0, src.width, src.height)

        for (i in pixels.indices) {
            val gray = android.graphics.Color.red(pixels[i]) // R=G=B after grayscale
            pixels[i] = if (gray > threshold)
                android.graphics.Color.WHITE
            else
                android.graphics.Color.BLACK
        }

        grayscale.setPixels(pixels, 0, src.width, 0, 0, src.width, src.height)

        return grayscale
    }
    fun showPrintPreviewDialog(finalBitmap: Bitmap) {

        val dialogView = layoutInflater.inflate(R.layout.dialog, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.imgPreview)
        imageView.setImageBitmap(finalBitmap)

        AlertDialog.Builder(this)
            .setTitle("Print Preview")
            .setView(dialogView)
            .setPositiveButton("Print") { _, _ ->
                tryPrint(finalBitmap)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun tryPrint(bitmap: Bitmap) {
        val open = printer.open()
        if (open != 0) {
            Toast.makeText(this, "Printer open failed: $open", Toast.LENGTH_LONG).show()
            return
        }

        try {
            // Page-based printing (common in Urovo SDK)
//            printer.setupPage(576, 300) // 58mm width usually ~576 dots



            printer.setupPage(384, bitmap.height + 10)
            printer.drawBitmap(bitmap,0,0)
//            printer.drawBitmapEx(bitmapToMonoBytes(bitmap), 0, 0, 384, bitmap.height)


//            printer.drawText(
//                "Hello Urovo i9100",
//                20, 20,
//                "DEFAULT", 24,
//                false, false, 0
//            )
//            printer.drawText(
//                "Flutter plugin test (native first)",
//                20, 70,
//                "DEFAULT", 18,
//                false, false, 0
//            )

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
    fun bitmapToMonoBytes(src: Bitmap, threshold: Int = 160): ByteArray {
        val width = src.width
        val height = src.height
        val widthBytes = (width + 7) / 8
        val out = ByteArray(widthBytes * height)

        var outIndex = 0
        for (y in 0 until height) {
            for (xb in 0 until widthBytes) {
                var b = 0
                for (bit in 0 until 8) {
                    val x = xb * 8 + bit
                    val isBlack = if (x < width) {
                        val p = src.getPixel(x, y)
                        val r = android.graphics.Color.red(p)
                        val g = android.graphics.Color.green(p)
                        val bl = android.graphics.Color.blue(p)
                        val gray = (0.299 * r + 0.587 * g + 0.114 * bl).toInt()
                        gray < threshold
                    } else {
                        false // pad white
                    }

                    if (isBlack) {
                        b = b or (1 shl (7 - bit))
                    }
                }
                out[outIndex++] = b.toByte()
            }
        }
        return out
    }
    fun toMono(src: Bitmap, threshold: Int = 160): Bitmap {
        val w = src.width
        val h = src.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val p = src.getPixel(x, y)
                val r = android.graphics.Color.red(p)
                val g = android.graphics.Color.green(p)
                val b = android.graphics.Color.blue(p)
                val gray = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
                out.setPixel(x, y, if (gray < threshold) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return out
    }
    fun scaleToWidth(src: Bitmap, targetW: Int): Bitmap {
        val h = (src.height * (targetW.toFloat() / src.width)).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(src, targetW, h, true)
    }
    fun toOpaqueWhite(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.RGB_565)
        val c = Canvas(out)
        c.drawColor(android.graphics.Color.WHITE)
        c.drawBitmap(src, 0f, 0f, null)
        return out
    }
    fun captureScreen(): Bitmap {
        val view = window.decorView.rootView
        val bitmap = createBitmap(view.width, view.height)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }
    fun toMonochrome(src: Bitmap): Bitmap {
        val width = src.width
        val height = src.height
        val bmp = createBitmap(width, height)

        for (x in 0 until width) {
            for (y in 0 until height) {
                val pixel = src[x, y]
                val r = C.red(pixel)
                val g = C.green(pixel)
                val b = C.blue(pixel)

                val gray = (0.3 * r + 0.59 * g + 0.11 * b).toInt()
                val newColor = if (gray > 128) C.WHITE else C.BLACK

                bmp[x, y] = newColor
            }
        }
        return bmp
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