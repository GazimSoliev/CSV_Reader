object HogwartsConverter {

    fun convert(dataTable: DataTable): Hogwarts? = runCatching {
        Hogwarts(
            students = dataTable.rows.toMutableList().apply {
                removeAt(0)
            }.map {
                val getString = { i: Int ->
                    it.cells[i].content
                }
                Student.getStudent(
                    gender = getString(0),
                    faculty = getString(1),
                    parentalLevelOfEducation = getString(2),
                    lunch = getString(3),
                    testPreparationCourse = getString(4),
                    charmsScore = getString(5),
                    potionsScore = getString(6),
                    darkArtsScore = getString(7)
                )
            }
        )
    }.getOrNull()

}

object CalculateAverage {

    private fun Hogwarts.calculateAverage(convert: (Student) -> Int?) =
        students.mapNotNull(convert).average()

    fun calculate(hogwarts: Hogwarts) = with(hogwarts) {
        listOf(
            calculateAverage(Student::charmsScore),
            calculateAverage(Student::potionsScore),
            calculateAverage(Student::darkArtsScore)
        )
    }

    fun countStudentPassObjectInScoreRange(hogwarts: Hogwarts, convert: (Student) -> Int?, scoreRange: IntRange) =
        hogwarts.students.filter { convert(it) in scoreRange }.size

    fun percentStudentPassObjectInScoreRange(hogwarts: Hogwarts, scoreRange: IntRange, convert: (Student) -> Int?) =
        hogwarts.students.let { students -> students.filter { convert(it) in scoreRange }.size.toFloat() / students.size }

    fun percentStudentPassObjectInScoreRange(
        hogwarts: Hogwarts,
        scoreRange: List<IntRange>,
        convert: (Student) -> Int?
    ) = scoreRange.map { percentStudentPassObjectInScoreRange(hogwarts, it, convert) }


}

data class Hogwarts(
    val students: List<Student>
)

data class Student(
    val gender: Gender,
    val faculty: Faculty,
    val parentalLevelOfEducation: String,
    val lunch: Lunch,
    val testPreparationCourse: PreparationCourse,
    val charmsScore: Int?,
    val potionsScore: Int?,
    val darkArtsScore: Int?
) {

    companion object {
        fun getStudent(
            gender: String,
            faculty: String,
            parentalLevelOfEducation: String,
            lunch: String,
            testPreparationCourse: String,
            charmsScore: String,
            potionsScore: String,
            darkArtsScore: String
        ) = Student(
            gender = Gender.getGender(gender),
            faculty = Faculty.getFaculty(faculty),
            parentalLevelOfEducation = parentalLevelOfEducation,
            lunch = Lunch.getLunch(lunch),
            testPreparationCourse = PreparationCourse.getPreparationCourse(testPreparationCourse),
            charmsScore = charmsScore.toIntOrNull(),
            potionsScore = potionsScore.toIntOrNull(),
            darkArtsScore = darkArtsScore.toIntOrNull()
        )

    }

}

enum class Lunch {
    Standard, FreeReduced, Unknown;

    companion object {
        fun getLunch(lunch: String) = when (lunch) {
            "standard" -> Standard
            "free/reduced" -> FreeReduced
            else -> Unknown
        }
    }
}

enum class Gender {
    Male, Female, Unknown;

    companion object {
        fun getGender(gender: String) = when (gender) {
            "male" -> Male
            "female" -> Female
            else -> Unknown
        }
    }
}

enum class Faculty {
    A, B, C, D, E, Unknown;

    companion object {
        fun getFaculty(faculty: String) = when (faculty.last()) {
            'A' -> A
            'B' -> B
            'C' -> C
            'D' -> D
            'E' -> E
            else -> Unknown
        }
    }
}

enum class PreparationCourse {
    Completed, None, Unknown;

    companion object {
        fun getPreparationCourse(preparationCourse: String) = when (preparationCourse) {
            "completed" -> Completed
            "none" -> None
            else -> Unknown
        }
    }
}