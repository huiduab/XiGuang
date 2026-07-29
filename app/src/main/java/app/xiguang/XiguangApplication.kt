package app.xiguang

import android.app.Application
import app.xiguang.data.local.XiguangDatabase
import app.xiguang.data.repository.CollectionRepository

class XiguangApplication : Application() {
    val database: XiguangDatabase by lazy {
        XiguangDatabase.create(this)
    }

    val collectionRepository: CollectionRepository by lazy {
        CollectionRepository(
            collectionDao = database.collectionDao(),
            folderDao = database.folderDao(),
        )
    }
}
