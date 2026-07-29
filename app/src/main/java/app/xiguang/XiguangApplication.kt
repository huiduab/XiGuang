package app.xiguang

import android.app.Application
import app.xiguang.data.file.LocalAttachmentStore
import app.xiguang.data.local.XiguangDatabase
import app.xiguang.data.preferences.SettingsRepository
import app.xiguang.data.repository.CollectionRepository
import app.xiguang.data.repository.ProjectRepository

class XiguangApplication : Application() {
    val database: XiguangDatabase by lazy {
        XiguangDatabase.create(this)
    }

    val collectionRepository: CollectionRepository by lazy {
        CollectionRepository(
            collectionDao = database.collectionDao(),
            folderDao = database.folderDao(),
            attachmentStore = LocalAttachmentStore(this),
        )
    }

    val projectRepository: ProjectRepository by lazy {
        ProjectRepository(database.projectDao(), database.sourceDao())
    }

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
}
