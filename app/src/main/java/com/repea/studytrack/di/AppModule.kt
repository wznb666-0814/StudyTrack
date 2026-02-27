package com.repea.studytrack.di

import android.content.Context
import androidx.room.Room
import com.repea.studytrack.data.local.AppDatabase
import com.repea.studytrack.data.local.dao.ExamRecordDao
import com.repea.studytrack.data.local.dao.SubjectDao
import com.repea.studytrack.data.local.dao.UserDao
import com.repea.studytrack.data.remote.DeepSeekClient
import com.repea.studytrack.repository.StudyRepository
import com.repea.studytrack.repository.StudyRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
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
        ).addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
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
    fun provideUserDao(database: AppDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideDeepSeekClient(okHttpClient: OkHttpClient): DeepSeekClient {
        return DeepSeekClient(okHttpClient)
    }

    @Provides
    @Singleton
    fun provideStudyRepository(
        subjectDao: SubjectDao,
        examRecordDao: ExamRecordDao,
        userDao: UserDao,
        userPreferencesRepository: com.repea.studytrack.repository.UserPreferencesRepository
    ): StudyRepository {
        return StudyRepositoryImpl(subjectDao, examRecordDao, userDao, userPreferencesRepository)
    }
}
