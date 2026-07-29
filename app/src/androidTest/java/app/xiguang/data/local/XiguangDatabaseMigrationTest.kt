package app.xiguang.data.local

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test

class XiguangDatabaseMigrationTest {
    @Test
    fun migration_from_v1_adds_unread_column_with_safe_default() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "migration-v1-v2-test.db"
        context.deleteDatabase(databaseName)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(V1Callback())
                .build(),
        )

        val database = helper.writableDatabase
        XiguangDatabase.MIGRATION_1_2.migrate(database)

        database.query("SELECT is_read FROM collections WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals(0, cursor.getInt(0))
        }
        helper.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration_from_v2_creates_projects_and_sources() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "migration-v2-v3-test.db"
        context.deleteDatabase(databaseName)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(databaseName).callback(V2Callback()).build(),
        )
        val database = helper.writableDatabase
        XiguangDatabase.MIGRATION_2_3.migrate(database)

        database.query("SELECT name FROM sqlite_master WHERE type = 'table' AND name IN ('projects', 'sources')").use { cursor ->
            assertEquals(2, cursor.count)
        }
        helper.close()
        context.deleteDatabase(databaseName)
    }
}

private class V1Callback : SupportSQLiteOpenHelper.Callback(1) {
    override fun onCreate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE collections (id INTEGER PRIMARY KEY, title TEXT NOT NULL)")
        db.execSQL("INSERT INTO collections (id, title) VALUES (1, 'Existing collection')")
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}

private class V2Callback : SupportSQLiteOpenHelper.Callback(2) {
    override fun onCreate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE collections (id INTEGER PRIMARY KEY, title TEXT NOT NULL, is_read INTEGER NOT NULL DEFAULT 0)")
    }

    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
}
