package com.sample.aijobassistant.di

import com.sample.aijobassistant.data.local.PdfDocumentTextExtractor
import com.sample.aijobassistant.data.local.SecureApiKeyStorage
import com.sample.aijobassistant.data.repository.ResumeAnalysisRepositoryImpl
import com.sample.aijobassistant.domain.repository.ApiKeyRepository
import com.sample.aijobassistant.domain.repository.DocumentTextExtractor
import com.sample.aijobassistant.domain.repository.ResumeAnalysisRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Binds domain-layer interfaces to data-layer implementations. This is the
 * single file that makes Clean Architecture's dependency inversion concrete:
 * use cases depend on the interfaces (left-hand side), Hilt injects the
 * implementations (right-hand side) at runtime, and nothing outside this
 * file needs to know which concrete class is in play.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindResumeAnalysisRepository(
        impl: ResumeAnalysisRepositoryImpl
    ): ResumeAnalysisRepository

    @Binds
    abstract fun bindApiKeyRepository(
        impl: SecureApiKeyStorage
    ): ApiKeyRepository

    @Binds
    abstract fun bindDocumentTextExtractor(
        impl: PdfDocumentTextExtractor
    ): DocumentTextExtractor
}
