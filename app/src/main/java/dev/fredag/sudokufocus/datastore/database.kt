package dev.fredag.sudokufocus.datastore

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.fredag.sudokufocus.model.Sudoku
import dev.fredag.sudokufocus.model.SudokuSource
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton // Tell Dagger-Hilt to create a singleton accessible everywhere in ApplicationCompenent (i.e. everywhere in the application)
    @Provides
    fun provideYourDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        AppDatabase::class.java,
        "sudoku_focus_db"
    )
        .fallbackToDestructiveMigration() // TODO this should be removed before release once schema is stable
        .build() // The reason we can construct a database for the repo

    @Singleton
    @Provides
    fun provideSudokuDao(db: AppDatabase) =
        db.sudokuDao() // The reason we can implement a Dao for the database
}

val format = Json { allowStructuredMapKeys = true }

class SudokuConverter {
    @TypeConverter
    fun sudokuFromString(string: String): Sudoku {
        return  string.let { format.decodeFromString(it) }
    }

    @TypeConverter
    fun sudokuToString(sudoku: Sudoku): String {
        return sudoku?.let { format.encodeToString(it) } ?: ""
    }
}

class SudokuSourceConverter {
    @TypeConverter
    fun sudokuSourceFromString(string: String): SudokuSource {
        return string?.let { Json.decodeFromString(string) }
    }

    @TypeConverter
    fun sudokuSourceToString(sudokuSource: SudokuSource): String {
        return Json.encodeToString(sudokuSource)
    }
}