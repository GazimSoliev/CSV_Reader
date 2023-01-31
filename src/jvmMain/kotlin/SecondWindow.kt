import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.window.Window
import com.example.compose.AppTheme
import org.jetbrains.skia.Font
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import java.awt.Dimension
import kotlin.math.round
import kotlin.random.Random

@Composable
fun HistogramWindow(
    title: String = "Histogram Window",
    onCloseRequest: () -> Unit = {},
    list: List<Float> = List(Random.nextInt(4, 25)) { round(Random.nextFloat() * 100) / 100 },
    listTitle: List<String> = list.map { it.toString() },
    maxNumber: Float = 100f
) {
    Window(onCloseRequest = onCloseRequest, title = title) {
        window.minimumSize = Dimension(800, 600)
        AppTheme {
            Histogram(list, listTitle, maxNumber)
        }
    }
}

@Preview
@Composable
fun Histogram(
    list: List<Float> = List(Random.nextInt(4, 25)) { round(Random.nextFloat() * 100) / 100 },
    listTitle: List<String> = list.map { it.toString() },
    maxNumber: Float = 100f
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        var surfaceSize by remember { mutableStateOf(Size.Zero) }
        Box(modifier = Modifier.fillMaxSize().padding(16.dp).onGloballyPositioned {
            surfaceSize = it.size.toSize()
        }) {
            val marginLeft =
                80f + (round(maxNumber * 100) / 100).toString().length * 6.5f + if (maxNumber >= 100f) 0f else 10f
            val marginHorizontalColumns = 16f
            val width =
                if (surfaceSize.width < marginLeft) 0f else (surfaceSize.width - marginLeft - marginHorizontalColumns * 2) / list.size
            val columnColor = MaterialTheme.colorScheme.primary
            val linesColor = MaterialTheme.colorScheme.onTertiaryContainer
            val rectColor = MaterialTheme.colorScheme.tertiaryContainer
            val numbersColor = MaterialTheme.colorScheme.onSecondaryContainer
            val backgroundNumbersColor = MaterialTheme.colorScheme.secondaryContainer
            val numbersColorOnColumn = MaterialTheme.colorScheme.primaryContainer
            val backgroundNumbersColorOnColumn = MaterialTheme.colorScheme.onPrimaryContainer
            val numbersColorOverColumn = MaterialTheme.colorScheme.surfaceVariant
            val backgroundNumbersColorOverColumn = MaterialTheme.colorScheme.onSurfaceVariant

            Column {
                Canvas(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    val paddingHorizontalColumn = 4f
                    val blockCount = 25
                    val blockHeight = size.height / blockCount
                    val paddingTop = blockHeight * 3
                    val paddingBottom = blockHeight * 2
                    val font = Font()
                    val paint0 = Paint().apply {
                        color = numbersColorOnColumn.toArgb()
                    }
                    val paint01 = Paint().apply {
                        color = numbersColorOverColumn.toArgb()
                    }
                    list.forEachIndexed { i, fl ->
                        val height = (size.height - paddingTop - paddingBottom) * fl
                        val topLeft = Offset(
                            x = marginHorizontalColumns + marginLeft + width * i + paddingHorizontalColumn,
                            y = size.height - height - paddingBottom
                        )
                        val rSize = Size(
                            if (width < paddingHorizontalColumn * 2) 0f else width - paddingHorizontalColumn * 2,
                            height
                        )
                        drawRoundRect(
                            color = columnColor, topLeft = topLeft, size = rSize, cornerRadius = CornerRadius(8f)
                        )
                        val textLine = TextLine.make((round(fl * maxNumber * 100) / 100).toString(), font)
                        if (rSize.width > (textLine.width + 30f)) drawIntoCanvas {
                            if (rSize.height > (textLine.capHeight + 40f)) {
                                drawRoundRect(
                                    color = backgroundNumbersColorOnColumn,
                                    topLeft = Offset(
                                        topLeft.x + (rSize.width - textLine.width) / 2 - 10f,
                                        topLeft.y + (rSize.height - textLine.capHeight) / 2 - 10f
                                    ),
                                    size = Size(textLine.width + 20f, textLine.capHeight + 20f),
                                    cornerRadius = CornerRadius(10f)
                                )
                                it.nativeCanvas.drawTextLine(
                                    line = textLine,
                                    x = topLeft.x + (rSize.width - textLine.width) / 2,
                                    y = topLeft.y + (rSize.height + textLine.capHeight) / 2,
                                    paint = paint0
                                )
                            } else {
                                val overColumn = 25f
                                drawRoundRect(
                                    color = backgroundNumbersColorOverColumn,
                                    topLeft = Offset(
                                        topLeft.x + (rSize.width - textLine.width) / 2 - 10f,
                                        topLeft.y - (textLine.capHeight) / 2 - 10f - overColumn
                                    ),
                                    size = Size(textLine.width + 20f, textLine.capHeight + 20f),
                                    cornerRadius = CornerRadius(10f)
                                )
                                it.nativeCanvas.drawTextLine(
                                    line = textLine,
                                    x = topLeft.x + (rSize.width - textLine.width) / 2,
                                    y = topLeft.y + (textLine.capHeight) / 2 - overColumn,
                                    paint = paint01
                                )
                            }
                        }
                    }
                    val linesWidth = 1.5f
                    val xMainLine = marginLeft - 25f
                    drawRoundRect(
                        color = rectColor,
                        topLeft = Offset(xMainLine - 25f, 0f),
                        size = Size(marginLeft - xMainLine + 25f, size.height),
                        cornerRadius = CornerRadius(25f)
                    )
                    val arrowSubWidth = 10f
                    val arrowHeight = if (arrowSubWidth * 2 < blockHeight) arrowSubWidth * 2 else blockHeight
                    drawLine(
                        start = Offset(x = xMainLine, y = blockHeight),
                        end = Offset(x = xMainLine + arrowSubWidth, y = blockHeight + arrowHeight),
                        color = linesColor,
                        strokeWidth = linesWidth
                    )
                    drawLine(
                        start = Offset(x = xMainLine, y = blockHeight),
                        end = Offset(x = xMainLine - arrowSubWidth, y = blockHeight + arrowHeight),
                        color = linesColor,
                        strokeWidth = linesWidth
                    )
                    drawLine(
                        start = Offset(x = xMainLine, y = blockHeight),
                        end = Offset(x = xMainLine, y = size.height - blockHeight),
                        color = linesColor,
                        strokeWidth = linesWidth
                    )
                    val paint1 = Paint().apply {
                        color = numbersColor.toArgb()
                    }
                    val double = size.height >= 800f
                    val lines = if (double) 20 else 10
                    var heightCount = size.height - paddingBottom
                    val subWidth = 10f
                    val stepHeight = if (double) blockHeight else blockHeight * 2
                    val step = maxNumber / lines
                    var numberCount = 0f
                    val convertIf = if (maxNumber >= 100f) { f: Float ->
                        round(f).toInt()
                    } else { f: Float -> round(f * 100) / 100 }
                    repeat(lines + 1) {
                        drawLine(
                            start = Offset(x = xMainLine - subWidth, y = heightCount),
                            end = Offset(x = xMainLine + subWidth, y = heightCount),
                            color = linesColor,
                            strokeWidth = linesWidth
                        )
                        drawIntoCanvas {
                            val textLine = TextLine.make(convertIf(numberCount).toString(), font)
                            val start = xMainLine - 45f - textLine.width
                            drawRoundRect(
                                color = backgroundNumbersColor,
                                topLeft = Offset(start - 10f, heightCount - textLine.capHeight / 2 - 10f),
                                size = Size(textLine.width + 20f, textLine.capHeight + 20f),
                                cornerRadius = CornerRadius(10f)
                            )
                            it.nativeCanvas.drawTextLine(
                                line = textLine,
                                x = start,
                                y = heightCount + textLine.capHeight / 2,
                                paint = paint1
                            )
                        }
                        heightCount -= stepHeight
                        numberCount += step
                    }
                }
                Spacer(Modifier.height(16.dp))
                Row {
                    Text(text = "Column titles:", modifier = Modifier.width(marginLeft.dp))
                    Spacer(modifier = Modifier.width(marginHorizontalColumns.dp))
                    listTitle.forEach {
                        Text(
                            text = it,
                            modifier = Modifier.width(width.dp).padding(start = 4.dp, end = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}