import java.io.File

object CSVReader {

    fun read(file: File) =
        DataTable(
            file.readLines().map {
                DataRow(cells = it.split(',').map(::DataCell))
            })

}

data class DataTable(
    val rows: List<DataRow>
)

data class DataRow(
    val cells: List<DataCell>
)

data class DataCell(
    val content: String
)