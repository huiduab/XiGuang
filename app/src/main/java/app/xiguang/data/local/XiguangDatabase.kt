package app.xiguang.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        CollectionEntity::class,
        FolderEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class XiguangDatabase : RoomDatabase() {
    abstract fun collectionDao(): CollectionDao
    abstract fun folderDao(): FolderDao

    companion object {
        fun create(context: Context): XiguangDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                XiguangDatabase::class.java,
                "xiguang.db",
            ).build()
    }
}
