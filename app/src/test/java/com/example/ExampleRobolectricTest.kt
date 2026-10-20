package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.PieceEntryEntity
import com.example.data.repository.PieceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: PieceRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = PieceRepository(db.pieceEntryDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("PieceRate", appName)
    }

    @Test
    fun `test piece entry calculation and CSV export format`() = runBlocking {
        val entry = PieceEntryEntity(
            piece = 25,
            color = "Navy Blue",
            pieceName = "8-Kali Kurti",
            size = "XL",
            price = 45.0,
            date = "2026-10-19"
        )
        // Verify 7. Total (₹) = Piece × Price
        assertEquals(1125.0, entry.total, 0.001)

        val id = repository.insert(entry)
        assertTrue(id > 0)

        val all = repository.allEntries.first()
        assertEquals(1, all.size)
        assertEquals("8-Kali Kurti", all[0].pieceName)

        // Verify CSV Export headers follow strict sequence
        val csv = repository.exportToCsv(all)
        assertTrue(csv.startsWith("Sr No,Piece (Qty),Color,Piece Name,Size,Price (Rate INR),Date,Total (INR)"))
        assertTrue(csv.contains("1,25,\"Navy Blue\",\"8-Kali Kurti\",\"XL\",45.0,\"2026-10-19\",1125.00"))
    }
}
