// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollableDefaults
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.compose.AppTheme
import java.awt.Dimension
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter


fun main() = application {
    var tableData by remember { mutableStateOf(DataTable(rows = emptyList())) }
    var hogwarts by remember { mutableStateOf<Hogwarts?>(null) }
    var state by remember { mutableStateOf(false) }
    Window(onCloseRequest = ::exitApplication, title = "Hogwarts") {
        window.minimumSize = Dimension(800, 600)
        App(onClick = {
            hogwarts = HogwartsConverter.convert(tableData)
            state = it
        }, onClick0 = { tableData = CSVReader.read(it) }, tableData = tableData
        )
    }
    if (state) {
        val listTitle = listOf(0..20, 21..40, 41..60, 61..80, 81..100)
        val onCloseRequest = { state = false }

        val hash = hashMapOf(
            Pair(first = "Charms Score", second = { s: Student -> s.charmsScore }),
            Pair(first = "Dark Arts Score", second = { s: Student -> s.darkArtsScore }),
            Pair(first = "Potions Score", second = { s: Student -> s.potionsScore }),
        )
        for (i in hash) {
            val result = hogwarts?.let {
                CalculateAverage.percentStudentPassObjectInScoreRange(
                    it,
                    listTitle, i.value
                )
            }
            if (result != null)
                HistogramWindow(
                    title = i.key,
                    onCloseRequest = onCloseRequest,
                    list = result,
                    listTitle = listTitle.map(IntRange::toString),
                    maxNumber = hogwarts?.students?.size?.toFloat() ?: 0f
                )
            else HistogramWindow(onCloseRequest = onCloseRequest)
        }


    }
}

@Composable
@Preview
fun App(onClick: (Boolean) -> Unit = {}, onClick0: (File) -> Unit = {}, tableData: DataTable = DataTable(emptyList())) {

    AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column {
                Row(modifier = Modifier.padding(16.dp).align(Alignment.CenterHorizontally)) {
                    OpenFileButton(onClick = onClick0)
                    Spacer(modifier = Modifier.run { padding(8.dp) })
                    OpenNewWindowButton(onClick)
                }
                Table(tableData)
            }
        }
    }
}

@Composable
fun OpenNewWindowButton(onClick: (Boolean) -> Unit) {
    Button(onClick = {
        onClick(true)
    }) {
        Text("Open histogram")
    }
}

@Composable
fun OpenFileButton(onClick: (File) -> Unit) {
    Button(onClick = { jFileChooser(onClick) }) {
        Text("Open file")
    }
}

fun jFileChooser(onSelected: (File) -> Unit) {
    val chooser = JFileChooser()
    val filter = FileNameExtensionFilter(
        "CSV, TXT", "csv", "txt"
    )
    chooser.fileFilter = filter
    val returnVal = chooser.showOpenDialog(null)
    if (returnVal == JFileChooser.APPROVE_OPTION) {
        onSelected(chooser.selectedFile)
    }
}

@Composable
@Preview
fun Table(dataTable: DataTable) {
    Box(modifier = Modifier.fillMaxSize()) {
        val state = rememberLazyListState()
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(8.dp)
        ) {
            SelectionContainer {
                LazyColumn(state = state) {
                    items(dataTable.rows) {
                        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                            it.cells.forEach { name ->
                                Text(
                                    text = name.content,
                                    modifier = Modifier.fillMaxHeight().weight(1f)
                                        .border(1.dp, MaterialTheme.colorScheme.outline).padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        VerticalScrollbar(
            adapter = rememberScrollbarAdapter(state),
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            style = defaultScrollbarStyle().copy(minimalHeight = 32.dp, thickness = 16.dp)
        )
    }

}

@Composable
fun LazyColumnWithSlider(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    reverseLayout: Boolean = false,
    verticalArrangement: Arrangement.Vertical = if (!reverseLayout) Arrangement.Top else Arrangement.Bottom,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    flingBehavior: FlingBehavior = ScrollableDefaults.flingBehavior(),
    userScrollEnabled: Boolean = true,
    content: LazyListScope.() -> Unit
) {

    Row(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            state = state,
            contentPadding = contentPadding,
            reverseLayout = reverseLayout,
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            flingBehavior = flingBehavior,
            userScrollEnabled = userScrollEnabled,
            content = content
        )
        var height0 by rememberSaveable { mutableStateOf(0f) }
        Box(modifier = Modifier.fillMaxHeight().width(20.dp).background(MaterialTheme.colorScheme.surfaceVariant)
            .onGloballyPositioned {
                height0 = it.size.height.toFloat()
            }) {
            var offsetY by remember { mutableStateOf(0f) }
            Box(modifier = Modifier.offset(y = offsetY.dp).background(Color.Red, RoundedCornerShape(4.dp))
                .fillMaxWidth().height(100.dp).pointerInput(Unit) {

                    detectVerticalDragGestures { change, dragAmount ->
//                            println(dragAmount)
//                            println(offsetY)
//                                offsetX += dragAmount.x
                        if ((offsetY + dragAmount) in 0f..(height0 - 100)) {
                            offsetY += dragAmount
                        } else if (dragAmount > 0) {
                            offsetY = height0 - 100
                        } else if (dragAmount < 0) {
                            offsetY = 0f
                        }
                        change.consume()
//                            offsetY += dragAmount
                    }
                })
        }
    }

}

@Composable
fun AppTest() {
    MaterialTheme {
        LazyColumnWithSlider(modifier = Modifier.fillMaxWidth()) {
            items(List(1000) { it.toString() }) {
                Text(it)
            }
        }
    }
}

@Composable
fun Float.fromPxToDp() = (this / LocalDensity.current.density).dp
