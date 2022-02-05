package dev.fredag.sudokufocus.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.fredag.sudokufocus.LocalSudokuRepository
import dev.fredag.sudokufocus.SudokuRepository

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModules {
    @Binds
    fun provideLocalSudokuRepository(repository: LocalSudokuRepository): SudokuRepository
}