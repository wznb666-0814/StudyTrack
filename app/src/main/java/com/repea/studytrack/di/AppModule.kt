package com.repea.studytrack.di

import android.content.Context
import androidx.room.Room
import com.repea.studytrack.data.local.AppDatabase
import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.repository.StudyRepository
import com.repea.studytrack.repository.StudyRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "study_track_db"
        ).build()
    }

    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    fun provideExamRecordDao(database: AppDatabase): ExamRecordDao {
        return database.examRecordDao()
    }

    @Provides
    @Singleton
    fun provideStudyRepository(
        subjectDao: SubjectDao,
        examRecordDao: ExamRecordDao
    ): StudyRepository {
        return StudyRepositoryImpl(subjectDao, examRecordDao)
    }
}
