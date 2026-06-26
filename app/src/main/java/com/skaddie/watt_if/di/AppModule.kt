package com.skaddie.watt_if.di

import android.content.Context
import androidx.room.Room
import com.skaddie.watt_if.data.datastore.UserPreferences
import com.skaddie.watt_if.data.local.WattIfDB
import com.skaddie.watt_if.data.local.dao.ReadingDao
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
    fun provideWattIfDB(@ApplicationContext context: Context): WattIfDB =
        Room.databaseBuilder(
            context,
            WattIfDB::class.java,
            "watt_if_db"
        ).build()

    @Provides
    @Singleton
    fun provideReadingDao(db: WattIfDB): ReadingDao = db.readingDao()

    @Provides
    @Singleton
    fun provideUserPreferences(@ApplicationContext context: Context): UserPreferences =
        UserPreferences(context)
}