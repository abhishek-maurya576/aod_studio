package com.aodstudio.app.di

import com.aodstudio.app.battery.BatteryRepository
import com.aodstudio.app.battery.BatteryRepositoryImpl
import com.aodstudio.app.data.repository.ThemeRepositoryImpl
import com.aodstudio.app.domain.repository.ThemeRepository
import com.aodstudio.app.media.MediaRepository
import com.aodstudio.app.media.MediaRepositoryImpl
import com.aodstudio.app.notification.NotificationRepository
import com.aodstudio.app.notification.NotificationRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for binding repository implementations to domain interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindThemeRepository(
        impl: ThemeRepositoryImpl
    ): ThemeRepository

    @Binds
    @Singleton
    abstract fun bindBatteryRepository(
        impl: BatteryRepositoryImpl
    ): BatteryRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(
        impl: NotificationRepositoryImpl
    ): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindMediaRepository(
        impl: MediaRepositoryImpl
    ): MediaRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: com.aodstudio.app.data.repository.SettingsRepositoryImpl
    ): com.aodstudio.app.domain.repository.SettingsRepository
}
