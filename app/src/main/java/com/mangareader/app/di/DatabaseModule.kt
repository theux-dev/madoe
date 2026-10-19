package com.mangareader.app.di

import android.content.Context
import androidx.room.Room
import com.mangareader.app.data.db.AppDatabase
import com.mangareader.app.data.db.BookmarkDao
import com.mangareader.app.data.db.CollectionDao
import com.mangareader.app.data.db.ComicDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "mangareader.db").build()

    @Provides fun provideComicDao(db: AppDatabase): ComicDao = db.comicDao()
    @Provides fun provideCollectionDao(db: AppDatabase): CollectionDao = db.collectionDao()
    @Provides fun provideBookmarkDao(db: AppDatabase): BookmarkDao = db.bookmarkDao()
}
