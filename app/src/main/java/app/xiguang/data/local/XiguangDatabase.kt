package app.xiguang.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CollectionEntity::class,
        FolderEntity::class,
        ProjectEntity::class,
        SourceEntity::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class XiguangDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun folderDao(): FolderDao
    abstract fun projectDao(): ProjectDao
    abstract fun sourceDao(): SourceDao

    companion object {
        fun create(context: Context): XiguangDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                XiguangDatabase::class.java,
                "xiguang.db",
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE collections ADD COLUMN is_read INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_projects_name ON projects (name)")
                db.execSQL("CREATE TABLE IF NOT EXISTS sources (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, project_id INTEGER, name TEXT NOT NULL, url TEXT, created_at INTEGER NOT NULL, updated_at INTEGER NOT NULL, FOREIGN KEY(project_id) REFERENCES projects(id) ON UPDATE NO ACTION ON DELETE SET NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_sources_project_id ON sources (project_id)")
            }
        }
    }
}
