class DotGame {
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

    class GameSymbols {
        companion object {
            const val EMPTY_CHAR = '.'
        }
    }

    class PlayerProperties {
        companion object {
            val playerCount = 2
            // Player count must be smaller than 10.
            // Because digitToChar() is used when assigning the number to the location.

            const val PLAYER_TURN_START_NUMBER = 1
            // Start from this number and go to the player count
        }
    }

    class GameFieldProperties {
        companion object {
            val gameFieldWidth = 5
            val gameFieldLength = 5
        }
    }

    companion object {
        val spaceCountBetweenCharactersOnScreen = 1 + GameFieldProperties.gameFieldWidth.toString().length
        /*
        This is used to define the space count between the characters on the screen dynamically
        by the width of the game field.

        Let's say the width of the game field is 10.
        There will be 3 (1 + 2) spaces between every character on the screen.
        If it was 1000, there would be 5 (1 + 4) spaces between every character on the screen.

        The length of the game field isn't used instead of the width of the game field,
        because the game field doesn't look good when the length is used.
         */
    }

    private val gameFieldList =
        Array(GameFieldProperties.gameFieldWidth) { Array(GameFieldProperties.gameFieldLength) { GameSymbols.EMPTY_CHAR } }
    private var gameStarted = false

    private var currentPlayerTurn = PlayerProperties.PLAYER_TURN_START_NUMBER
    private val currentPlayerTurnChar: Char
        get() = currentPlayerTurn.digitToChar()

    //<editor-fold desc="Screen Methods">

    fun printWelcomeScreen() {
        println(">!>!> Welcome to the 3 Dot Game <!<!<")
		println("\nGame Description :")
		println("The first player to place 3 consecutive dots in any direction wins!")
        pauseScreenFor(4)
    }

    fun printGameField() {

        repeat(spaceCountBetweenCharactersOnScreen + 1) { print(" ") }
        // Add one more space to make the game field look better

        repeat(GameFieldProperties.gameFieldLength)
        {
            print(it + 1) // Print the column headers
            // I used the addition of 1 all along this method to make expressing a location easier for the player.

            repeat(spaceCountBetweenCharactersOnScreen - (lengthOf(it + 1) - 1)) { print(" ") }
            // The reason for subtraction from the space count is to print the space count dynamically.
            // For example, if the space count is 2, there will be 2 spaces after a one-digit column header.
            // But, there must be 1 space after a two-digit column header.

            /*
            length - 1 prevents decreasing the space count by an extra one.
            For example, 5 is a 1-digit number and its length is 1. It would decrease the space count by one.
            But it doesn't since there is a subtraction from 1.

            (it+1) is used to get the printed number on the screen. It prevents getting its index.
            */
        }

        println()

        for (verticalIndex in 0 until GameFieldProperties.gameFieldWidth) {
            print(verticalIndex + 1)    // Print the row headers

            repeat(spaceCountBetweenCharactersOnScreen - (lengthOf(verticalIndex + 1) - 1)) { print(" ") }

            for (horizontalIndex in 0 until GameFieldProperties.gameFieldLength) {
                print(gameFieldList[verticalIndex][horizontalIndex])
                // Print the elements of the game field

                repeat(spaceCountBetweenCharactersOnScreen) { print(" ") }
            }
            println()
        }
    }

    fun printCurrentPlayerTurn() {
        println("\n>>Now, Player $currentPlayerTurn's turn.")
    }

    fun clearScreen() {
        // There was no supported way to do this, so I did my own.
        repeat(50) {
            println()
        }
    }

    fun pauseScreenFor(seconds: Int) {
        if (seconds > 0) {
            Thread.sleep((seconds * 1000).toLong())
        }
    }

    //</editor-fold>

    fun getDotLocationIndexInput(): Array<Int> {
        var columnNumber: Int
        var rowNumber: Int

        while (true) {
            print("\n>Please enter a row number : ")

            rowNumber = (readLine()?.toIntOrNull() ?: 0) - 1

            if (rowNumber in (0 until GameFieldProperties.gameFieldWidth)) {
                break
            } else {
                println("\n>!>Please enter a row number between 1 and ${GameFieldProperties.gameFieldWidth}.")
            }

        }

        while (true) {
            print("\n>Please enter a column number : ")

            columnNumber = (readLine()?.toIntOrNull() ?: 0) - 1
            // The reason for subtraction from 1 is for array indexing


            if (columnNumber in (0 until GameFieldProperties.gameFieldLength)) {
                break
            } else {
                println("\n>!>Please enter a column number between 1 and ${GameFieldProperties.gameFieldLength}.")
            }
        }

        return arrayOf(rowNumber, columnNumber)
    }

    fun placeDotAt(verticalIndex: Int, horizontalIndex: Int): Int {
        if (gameStarted) {
            if (gameFieldList[verticalIndex][horizontalIndex] == GameSymbols.EMPTY_CHAR) {
                if (isThereAnyAdjacentDotAt(verticalIndex, horizontalIndex)) {
                    gameFieldList[verticalIndex][horizontalIndex] = currentPlayerTurnChar
                    // Use the current player turn number as the placed symbol.
                } else {
                    return PlacingDotResults.NO_ADJACENT_DOT
                }
            } else {
                return PlacingDotResults.ALREADY_FILLED
            }
        } else    // You can place the first dot anywhere for the first turn
        {
            gameFieldList[verticalIndex][horizontalIndex] = currentPlayerTurnChar

            gameStarted = true
        }

        return PlacingDotResults.SUCCESS
    }

    fun isThereAnyEmptySpaceLeft(): Boolean {
        // This method is used to check if there is a draw or not.
        gameFieldList.forEach { it: Array<Char> ->
            it.forEach {
                if (it == GameSymbols.EMPTY_CHAR) {
                    return true
                }
            }
        }
        return false
    }

    fun changeCurrentPlayerTurnToNext() {
        if (currentPlayerTurn < PlayerProperties.playerCount) {
            currentPlayerTurn++
        } else {
            currentPlayerTurn = PlayerProperties.PLAYER_TURN_START_NUMBER
        }
    }

    fun didCurrentPlayerWin(): Boolean {
        for (verticalIndex in 0 until GameFieldProperties.gameFieldWidth) {
            for (horizontalIndex in 0 until GameFieldProperties.gameFieldLength) {

                if (gameFieldList[verticalIndex][horizontalIndex] == currentPlayerTurnChar) {
                    // Vertical Combination, check from up to down
                    if (((gameFieldList.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                        && (gameFieldList.getOrNull(verticalIndex + 2)?.getOrNull(horizontalIndex)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                    ) {
                        return true
                    }

                    // Horizontal Combination, check from left to right
                    if (((gameFieldList.getOrNull(verticalIndex)?.getOrNull(horizontalIndex + 1)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                        && (gameFieldList.getOrNull(verticalIndex)?.getOrNull(horizontalIndex + 2)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                    ) {
                        return true
                    }

                    // Cross ( / ) Combination, check from top-right to bottom-left
                    if (((gameFieldList.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex - 1)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                        && (gameFieldList.getOrNull(verticalIndex + 2)?.getOrNull(horizontalIndex - 2)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                    ) {
                        return true
                    }

                    // Back Cross ( \ ) Combination, check from top-left to bottom-right
                    if (((gameFieldList.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex + 1)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar)
                        && (gameFieldList.getOrNull(verticalIndex + 2)?.getOrNull(horizontalIndex + 2)
                            ?: GameSymbols.EMPTY_CHAR) == currentPlayerTurnChar
                    ) {
                        return true
                    }
                }
            }

            return false
        }

        return true
    }

    fun printGameEndResult(result: Int) {
        if (result == GameEndResults.CURRENT_PLAYER_WIN) {
            println("\n>!>!> Player $currentPlayerTurnChar win! <!<!<")
        } else {
            println("\n>!>!> Draw <!<!<")
        }
    }


    private fun isThereAnyAdjacentDotAt(verticalIndex: Int, horizontalIndex: Int): Boolean
    // This method checks if there is any adjacent dot, it doesn't matter if it's from different players.
    {

        return when {
            (gameFieldList.getOrNull(verticalIndex)?.getOrNull(horizontalIndex + 1)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true    // Right

            (gameFieldList.getOrNull(verticalIndex)?.getOrNull(horizontalIndex - 1)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true    // Left


            (gameFieldList.getOrNull(verticalIndex - 1)?.getOrNull(horizontalIndex)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true // Up

            (gameFieldList.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true // Down


            (gameFieldList.getOrNull(verticalIndex - 1)?.getOrNull(horizontalIndex + 1)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true  // Right Top Cross

            (gameFieldList.getOrNull(verticalIndex - 1)?.getOrNull(horizontalIndex - 1)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true  // Left Top Cross


            (gameFieldList.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex + 1)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true  // Right Bottom Cross

            (gameFieldList.getOrNull(verticalIndex + 1)?.getOrNull(horizontalIndex - 1)
                ?: GameSymbols.EMPTY_CHAR)
                    != GameSymbols.EMPTY_CHAR -> true  // Left Bottom Cross

            else -> false
        }
    }

    private fun lengthOf(number: Int): Int {
        return number.toString().length
    }
}

fun main() {
    val myDotGame = DotGame()

    myDotGame.printWelcomeScreen()
    myDotGame.clearScreen() // It's used to clear the welcome screen

    while (true) {
        myDotGame.printGameField()
        myDotGame.printCurrentPlayerTurn()

        val currentInput: Array<Int> = myDotGame.getDotLocationIndexInput()

        when (myDotGame.placeDotAt(currentInput[0], currentInput[1])) {
            DotGame.PlacingDotResults.SUCCESS -> {

                if (myDotGame.didCurrentPlayerWin()) {
                    myDotGame.clearScreen()
                    myDotGame.printGameField()
                    myDotGame.printGameEndResult(DotGame.GameEndResults.CURRENT_PLAYER_WIN)
                    break
                }

                if (!myDotGame.isThereAnyEmptySpaceLeft()) {
                    myDotGame.clearScreen()
                    myDotGame.printGameField()
                    myDotGame.printGameEndResult(DotGame.GameEndResults.DRAW)
                    break
                } else {
                    myDotGame.changeCurrentPlayerTurnToNext()
                }
            }

            DotGame.PlacingDotResults.ALREADY_FILLED -> {
                println("\n>>This location is already filled! Row[${currentInput[0] + 1}] Column[${currentInput[1] + 1}]")
                myDotGame.pauseScreenFor(3)
            }
            DotGame.PlacingDotResults.NO_ADJACENT_DOT -> {
                println("\n>>There was no adjacent dot! Row[${currentInput[0] + 1}] Column[${currentInput[1] + 1}]")
                myDotGame.pauseScreenFor(3)
            }
        }

        myDotGame.clearScreen()
    }
}
