package dev.fredag.sudokufocus

import org.junit.Assert.assertNotNull
import org.junit.Test

class LocalSudokuRepositoryTest {

    @Test
    fun getCampaignSudokus() {
        getValidatedSudokus().map { assertNotNull(it) }
    }
}