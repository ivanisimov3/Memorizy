package com.example.memorizy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.memorizy.data.AppDatabase
import com.example.memorizy.data.card.Card
import com.example.memorizy.data.card.CardDao
import com.example.memorizy.data.studyset.StudySet
import com.example.memorizy.data.studyset.StudySetDao
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var studySetDao: StudySetDao
    private lateinit var cardDao: CardDao

    // Создаем фальшивую бд
    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Используем inMemoryDatabaseBuilder, чтобы база не сохранялась на диске
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).build()

        studySetDao = db.studySetDao()
        cardDao = db.cardDao()
    }

    // закрываем базу данных после каждого теста
    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertSet_and_getSetById() = runBlocking {
        // Arrange (Подготовка)
        val testSet = StudySet(name = "Английский", description = "A1", iconId = 1, id = 1)

        // Act (Действие)
        studySetDao.insertSet(testSet)

        // Assert (Проверка)
        // .first() получает первое значение из Flow
        val retrievedSet = studySetDao.getSet(1).first()
        assertEquals(testSet.name, retrievedSet.name)
    }

    @Test
    @Throws(Exception::class)
    fun getAllSetsWithCardCount_returnsCorrectCount() = runBlocking {
        // Arrange
        val set1 = StudySet(name = "Set 1", description = null, iconId = 1, id = 1)
        val set2 = StudySet(name = "Set 2", description = null, iconId = 2, id = 2)
        studySetDao.insertSet(set1)
        studySetDao.insertSet(set2)

        cardDao.insertCard(Card(term = "t1", definition = "d1", setId = 1))
        cardDao.insertCard(Card(term = "t2", definition = "d2", setId = 1))

        // Act
        val setsWithCounts = studySetDao.getAllSetsWithCardNumber().first()

        // Assert
        val retrievedSet1 = setsWithCounts.find { it.studySet.id == 1 }
        val retrievedSet2 = setsWithCounts.find { it.studySet.id == 2 }

        assertEquals(2, retrievedSet1?.cardNumber) // У набора 1 должно быть 2 карточки
        assertEquals(0, retrievedSet2?.cardNumber) // У набора 2 должно быть 0 карточек
    }

    @Test
    @Throws(Exception::class)
    fun deleteSet_cascadesDeleteToCards() = runBlocking {
        // Arrange
        val set1 = StudySet(name = "Set 1", description = null, iconId = 1, id = 1)
        studySetDao.insertSet(set1)
        cardDao.insertCard(Card(term = "t1", definition = "d1", setId = 1, id = 10))

        // Убедимся, что все на месте
        assertEquals(1, cardDao.getAllCardsForSet(1).first().size)

        // Act (Удаляем НАБОР)
        studySetDao.deleteSet(set1)

        // Assert (Проверяем, что и карточки удалились)
        assertEquals(0, cardDao.getAllCardsForSet(1).first().size) // Должно быть 0
        assertEquals(null, studySetDao.getSet(1).first()) // Flow вернет null, если ничего нет
    }
}