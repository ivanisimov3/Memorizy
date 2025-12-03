package com.example.memorizy

import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.repository.StudySetRepositoryImpl
import com.example.memorizy.data.repository.StudySetRepository
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class StudySetRepositoryImplTest {
    // Эта "магия" от MockK автоматически создает моки
    @get:Rule
    val mockkRule = MockKRule(this)

    // Создаем "фальшивый" DAO.
    // @RelaxedMockK значит, что нам не нужно описывать поведение для каждой его функции
    @RelaxedMockK   // Позволяет проверять какие методы были вызваны у Dao
    private lateinit var studySetDao: StudySetDao

    // Наш класс, который мы будем тестировать
    private lateinit var studySetRepository: StudySetRepository

    @Before
    fun setUp() {
        // Перед каждым тестом мы создаем *настоящий* репозиторий,
        // передавая в него *фальшивый* DAO
        studySetRepository = StudySetRepositoryImpl(studySetDao)
    }

    // Тест: Проверяем, что вызов insertSet у репозитория
    // вызывает insertSet у DAO
    @Test
    fun `insertSet calls dao insertSet`() = runTest { // runTest - для корутин в тестах
        // Arrange (Подготовка)
        val testSet = StudySet(id = 1, name = "Test", description = null, iconId = 1)

        // Act (Действие)
        studySetRepository.insertSet(testSet)

        // Assert (Проверка)
        // Мы проверяем, что у studySetDao была вызвана
        // suspend-функция insertSet() ровно 1 раз
        coVerify(exactly = 1) { studySetDao.insertSet(testSet) }
    }

    @Test
    fun `deleteSet calls dao deleteSet`() = runTest {
        // Arrange
        val testSet = StudySet(id = 1, name = "Test", description = null, iconId = 1)

        // Act
        studySetRepository.deleteSet(testSet)

        // Assert
        coVerify(exactly = 1) { studySetDao.deleteSet(testSet) }
    }

    // Тесты для Flow-функций (getAllSets, getSetById) делать не нужно,
    // так как они просто возвращают то, что вернул DAO.
    // Мы тестируем *нашу* логику, а там ее нет (просто `return dao.xyz()`).
    // Мы уже протестировали DAO в `AppDatabaseTest`.
}