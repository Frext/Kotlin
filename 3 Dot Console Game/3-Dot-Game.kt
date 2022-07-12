import kotlin.system.exitProcess

class DotGame {

    class PlayerProperties {
        companion object {
            val maximumPlayerCount = 2
                get() = field.coerceIn(1..9)
            // Player count has to be smaller than or equal to 9.
            // Because digitToChar() is used when assigning the number to the location.

            const val PLAYER_TURN_START_NUMBER = 1
            // Start from this player number and increase until the max player count by each player's turn,
            // and then get back to this player number. Then, iterate that.
        }
    }

    class GameFieldProperties {
        companion object {
            val gameFieldWidth = 5
                get() = field.coerceAtLeast(1)

            val gameFieldLength = 5
                get() = field.coerceAtLeast(1)
        }
    }

    class PlacingDotResults {
        companion object {
            const val SUCCESS = 1
            const val ALREADY_FILLED = 2
            const val NO_ADJACENT_DOT = 3
        }
    }

    class GameEndResults {
        companion object {
            const val CURRENT_PLAYER_WIN = 1
            const val DRAW = 2
        }
    }

    class ScreenSymbols {
        companion object {
            const val EMPTY_CHAR = '.'

            // These below are for strings printed on the console
            const val ERROR_PREFIX = ">!>"
            const val REGULAR_PREFIX = ">>>"
            const val ERROR_SUFFIX = "<!<"
            const val REGULAR_SUFFIX = "<<<"
        }
    }

    class ErrorMessages {   // This class contains error messages for methods that mustn't be called before the game is started.
        companion object {
            private const val usedBeforeGameStarted = " is used before the game started!"

            const val adjacentDotMethodViolation =
                "\n${ScreenSymbols.ERROR_PREFIX} The method to search for an adjacent dot " +
                        "$usedBeforeGameStarted ${ScreenSymbols.ERROR_SUFFIX}"

            const val changePlayerTurnMethodViolation =
                "\n${ScreenSymbols.ERROR_PREFIX} The method to change the player turn" +
                        "$usedBeforeGameStarted ${ScreenSymbols.ERROR_SUFFIX}"

            const val didPlayerWinMethodViolation =
                "\n${ScreenSymbols.ERROR_PREFIX} The method to check if the player win" +
                        "$usedBeforeGameStarted ${ScreenSymbols.ERROR_SUFFIX}"

            const val emptySpaceLeftMethodViolation =
                "\n${ScreenSymbols.ERROR_PREFIX} The method to check if there is a draw" +
                        "$usedBeforeGameStarted ${ScreenSymbols.ERROR_SUFFIX}"

            const val printEndResultMethodViolation =
                "\n${ScreenSymbols.ERROR_PREFIX} The method to print the game end result" +
                        "$usedBeforeGameStarted ${ScreenSymbols.ERROR_SUFFIX}"
        }
    }

    companion object {
        val spaceCountBetweenCharactersOnScreen = GameFieldProperties.gameFieldWidth.toString().length + 1
        /*
        This is used to define the space count between the characters on the console dynamically
        by the width of the game field.

        Let's say the width of the game field is 10.
        There will be 3 (2 + 1) spaces between every character on the console.
        If it was 1000, there would be 5 (4 + 1) spaces between every character on the console.

        The length of the game field isn't used instead of the width of the game field,
        because the game field doesn't look good when the length is used for this.
         */
    }

    private val gameField =
        Array(GameFieldProperties.gameFieldWidth) { Array(GameFieldProperties.gameFieldLength) { ScreenSymbols.EMPTY_CHAR } }

    private var isFirstDotPlaced = false

    private var currentPlayerTurn = PlayerProperties.PLAYER_TURN_START_NUMBER
    private val currentPlayerTurnChar: Char
        get() = currentPlayerTurn.digitToChar()


    fun printWelcomeScreenForSeconds() {
        println("${ScreenSymbols.REGULAR_PREFIX} Welcome to the 3 Dot Game! ${ScreenSymbols.REGULAR_SUFFIX}")
        pauseScreenFor(2)
    }

    fun printGameField() {
        repeat(spaceCountBetweenCharactersOnScreen + 1) { print(" ") }
        // Add one more space to align the top location headers part to the game field.

        for (columnHeader in 1..GameFieldProperties.gameFieldWidth) {

            print(columnHeader)

            repeat(spaceCountBetweenCharactersOnScreen - (lengthOf(columnHeader) - 1)) { print(" ") }
            // The reason for subtraction from the space count is to print the space count dynamically.
            // For example, if the space count is 2, there will be 2 spaces after a one-digit column header.
            // But, there must be 1 space after a two-digit column header.

            // length - 1 prevents decreasing the space count by an extra one.
            // For example, 5 is a 1-digit number and its length is 1. It would decrease the space count by one.
            // But it doesn't since there is a subtraction from 1.
        }

        println()

        for (rowHeader in 1..GameFieldProperties.gameFieldWidth) {

            print(rowHeader)

            repeat(spaceCountBetweenCharactersOnScreen - (lengthOf(rowHeader) - 1)) { print(" ") }

            for (columnHeader in 1..GameFieldProperties.gameFieldLength) {
                print(gameField[columnHeader - 1][rowHeader - 1])
                // Print the elements of the game field

                repeat(spaceCountBetweenCharactersOnScreen) { print(" ") }
            }

            println()
        }
    }

    fun printCurrentPlayerTurn() {
        println("\n${ScreenSymbols.REGULAR_PREFIX} Now, Player $currentPlayerTurn's turn.")
    }

    fun clearConsole() {
        // There was no supported way to clean the console, so I did my own.
        repeat(50) {
            println()
        }
    }

    fun pauseScreenFor(seconds: Int) {
        if (seconds > 0) {
            Thread.sleep((seconds * 1000).toLong())
        }
    }

    fun getDotLocationIndexInput(): Array<Int> {
        return arrayOf(getRowIndexInput(), getColumnIndexInput())
    }

    private fun getRowIndexInput(): Int {
        var rowIndex: Int

        while (true) {
            print("\n${ScreenSymbols.REGULAR_PREFIX} Please enter a row number : ")

            rowIndex = (readLine()?.toIntOrNull() ?: 0) - 1
            // The reason for subtraction from 1 is for array indexing

            if (rowIndex in (0 until GameFieldProperties.gameFieldWidth)) {
                break
            } else {
                println("\n${ScreenSymbols.ERROR_PREFIX} Please enter a row number between 1 and ${GameFieldProperties.gameFieldWidth}.")
            }
        }

        return rowIndex
    }

    private fun getColumnIndexInput(): Int {
        var columnIndex: Int

        while (true) {
            print("\n${ScreenSymbols.REGULAR_PREFIX} Please enter a column number : ")

            columnIndex = (readLine()?.toIntOrNull() ?: 0) - 1
            // The reason for subtraction from 1 is for array indexing


            if (columnIndex in (0 until GameFieldProperties.gameFieldLength)) {
                break
            } else {
                println("\n${ScreenSymbols.REGULAR_PREFIX} Please enter a column number between 1 and ${GameFieldProperties.gameFieldLength}.")
            }
        }

        return columnIndex
    }

    fun placeDotAt(verticalIndex: Int, horizontalIndex: Int): Int {
        if (isFirstDotPlaced) {

            if (gameField[verticalIndex][horizontalIndex] == ScreenSymbols.EMPTY_CHAR) {

                if (isThereAnyAdjacentDotAt(verticalIndex, horizontalIndex)) {
                    gameField[verticalIndex][horizontalIndex] = currentPlayerTurnChar
                    // Use the current player turn number as the placed symbol.
                } else {
                    return PlacingDotResults.NO_ADJACENT_DOT
                }

            } else {
                return PlacingDotResults.ALREADY_FILLED
            }
        } else    // You can place the first dot anywhere for the first turn
        {
            gameField[verticalIndex][horizontalIndex] = currentPlayerTurnChar

            isFirstDotPlaced = true
        }

        return PlacingDotResults.SUCCESS
    }

    private fun isThereAnyAdjacentDotAt(verticalIndex: Int, horizontalIndex: Int): Boolean
    // This method checks if there is any adjacent dot, it doesn't matter if it's from different players.
    {
        if (isFirstDotPlaced) {
            return when {
                (gameField.getOrNull(verticalIndex)?.getOrNull(horizontalIndex + 1)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true    // Right

                (gameField.getOrNull(verticalIndex)?.getOrNull(horizontalIndex - 1)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true    // Left


                (gameField.getOrNull(verticalIndex - 1)?.getOrNull(horizontalIndex)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true // Up

                (gameField.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true // Down


                (gameField.getOrNull(verticalIndex - 1)?.getOrNull(horizontalIndex + 1)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true  // Right Top Cross

                (gameField.getOrNull(verticalIndex - 1)?.getOrNull(horizontalIndex - 1)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true  // Left Top Cross


                (gameField.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex + 1)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true  // Right Bottom Cross

                (gameField.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex - 1)
                    ?: ScreenSymbols.EMPTY_CHAR)
                        != ScreenSymbols.EMPTY_CHAR -> true  // Left Bottom Cross


                else -> false
            }
        }

        println(ErrorMessages.adjacentDotMethodViolation)
        terminateProgram()
        return false
    }

    fun isEveryCellFilled(): Boolean { // This method is used to check if there is a draw or not.
        if (isFirstDotPlaced) {
            gameField.forEach { it: Array<Char> ->  // The first "it" represents the horizontal index.
                it.forEach {    // The second "it" represents the vertical index.
                    if (it == ScreenSymbols.EMPTY_CHAR) {
                        return false
                    }
                }
            }

            return true
        }

        println(ErrorMessages.emptySpaceLeftMethodViolation)
        terminateProgram()
        return false
    }

    fun changeCurrentPlayerTurnToNext() {
        if (isFirstDotPlaced) {

            if (currentPlayerTurn < PlayerProperties.maximumPlayerCount) {
                currentPlayerTurn++
            } else {
                currentPlayerTurn = PlayerProperties.PLAYER_TURN_START_NUMBER
            }
        }

        println(ErrorMessages.changePlayerTurnMethodViolation)
        terminateProgram()
    }

    fun didCurrentPlayerWin(): Boolean {
        if (isFirstDotPlaced) {
            for (verticalIndex in 0 until GameFieldProperties.gameFieldWidth) {
                for (horizontalIndex in 0 until GameFieldProperties.gameFieldLength) {

                    if (gameField[verticalIndex][horizontalIndex] == currentPlayerTurnChar) {
                        // Vertical Combination, check from up to down
                        if (((gameField.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                            && (gameField.getOrNull(verticalIndex + 2)?.getOrNull(horizontalIndex)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                        ) {
                            return true
                        }

                        // Horizontal Combination, check from left to right
                        if (((gameField.getOrNull(verticalIndex)?.getOrNull(horizontalIndex + 1)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                            && (gameField.getOrNull(verticalIndex)?.getOrNull(horizontalIndex + 2)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                        ) {
                            return true
                        }

                        // Cross ( / ) Combination, check from top-right to bottom-left
                        if (((gameField.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex - 1)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                            && (gameField.getOrNull(verticalIndex + 2)?.getOrNull(horizontalIndex - 2)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                        ) {
                            return true
                        }

                        // Back Cross ( \ ) Combination, check from top-left to bottom-right
                        if (((gameField.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex + 1)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                            && (gameField.getOrNull(verticalIndex + 2)?.getOrNull(horizontalIndex + 2)
                                ?: ScreenSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                        ) {
                            return true
                        }
                    }
                }

                return false
            }

            return true
        }

        println(ErrorMessages.didPlayerWinMethodViolation)
        terminateProgram()
        return false
    }

    fun printGameEndResult(result: Int) {
        if (isFirstDotPlaced) {
            if (result == GameEndResults.CURRENT_PLAYER_WIN) {
                println("\n>!>!> Player $currentPlayerTurnChar win! <!<!<")
            } else {
                println("\n>!>!> Draw <!<!<")
            }
        }

        println(ErrorMessages.printEndResultMethodViolation)
        terminateProgram()
    }

    private fun lengthOf(number: Int): Int {
        return number.toString().length
    }

    private fun terminateProgram() {
        exitProcess(0)
    }
}

fun main() {
    val myDotGame = DotGame()

    myDotGame.printWelcomeScreenForSeconds()
    myDotGame.clearConsole() // This is used to clear the welcome screen

    while (true) {
        myDotGame.printGameField()
        myDotGame.printCurrentPlayerTurn()

        val currentInput: Array<Int> = myDotGame.getDotLocationIndexInput()

        when (myDotGame.placeDotAt(currentInput[0], currentInput[1])) {

            DotGame.PlacingDotResults.SUCCESS -> {

                if (myDotGame.didCurrentPlayerWin()) {
                    myDotGame.clearConsole()
                    myDotGame.printGameField()
                    myDotGame.printGameEndResult(DotGame.GameEndResults.CURRENT_PLAYER_WIN)
                    break
                }

                if (myDotGame.isEveryCellFilled()) {
                    myDotGame.clearConsole()
                    myDotGame.printGameField()
                    myDotGame.printGameEndResult(DotGame.GameEndResults.DRAW)
                    break
                } else {
                    myDotGame.changeCurrentPlayerTurnToNext()
                }
            }

            DotGame.PlacingDotResults.ALREADY_FILLED -> {
                println("\n${DotGame.ScreenSymbols.ERROR_PREFIX} This location is already filled! Row[${currentInput[0] + 1}] Column[${currentInput[1] + 1}]")
                myDotGame.pauseScreenFor(3)
            }

            DotGame.PlacingDotResults.NO_ADJACENT_DOT -> {
                println("\n${DotGame.ScreenSymbols.ERROR_PREFIX} There was no adjacent dot! Row[${currentInput[0] + 1}] Column[${currentInput[1] + 1}]")
                myDotGame.pauseScreenFor(3)
            }

        }
        myDotGame.clearConsole()
    }
}
