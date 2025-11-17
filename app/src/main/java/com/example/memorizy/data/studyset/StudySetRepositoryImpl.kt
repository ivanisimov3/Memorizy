package com.example.memorizy.data.studyset

import com.example.memorizy.data.StudySetWithCardNumber
import com.example.memorizy.domain.studyset.StudySetRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

// Конкретная реализация для работы с StudySetDao
class StudySetRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: StudySetDao
) : StudySetRepository {
    override suspend fun insertSet(studySet: StudySet) {
        return dao.insertSet(studySet)
    }

    override suspend fun deleteSet(studySet: StudySet) {
        return dao.deleteSet(studySet)
    }

    override fun getSet(setId: Int): Flow<StudySet> {
        return dao.getSet(setId)
    }

    override fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>> {
        return dao.getAllSetsWithCardNumber()
    }
}