package app.xiguang.data.repository

import app.xiguang.data.local.ProjectDao
import app.xiguang.data.local.ProjectEntity
import app.xiguang.data.local.SourceDao
import app.xiguang.data.local.SourceEntity
import app.xiguang.domain.model.Project
import app.xiguang.domain.model.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class ProjectRepository(
    private val projectDao: ProjectDao,
    private val sourceDao: SourceDao,
) {
    fun observeProjects(): Flow<List<Project>> = combine(projectDao.observeAll(), sourceDao.observeAll()) { projects, sources ->
        projects.map { project ->
            Project(project.id, project.name, project.description, sources.filter { it.projectId == project.id }.map { it.toDomain() })
        }
    }

    fun observeUnassignedSources(): Flow<List<Source>> = sourceDao.observeAll().combine(projectDao.observeAll()) { sources, projects ->
        val projectIds = projects.mapTo(mutableSetOf(), ProjectEntity::id)
        sources.filter { it.projectId == null || it.projectId !in projectIds }.map { it.toDomain() }
    }

    suspend fun saveProject(id: Long?, name: String, description: String?): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        val now = System.currentTimeMillis()
        return try {
            if (id == null) projectDao.insert(ProjectEntity(name = trimmed, description = description?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)) > 0
            else projectDao.update(id, trimmed, description?.trim()?.ifBlank { null }, now) > 0
        } catch (_: android.database.sqlite.SQLiteConstraintException) { false }
    }

    suspend fun deleteProject(id: Long): Boolean = projectDao.delete(id) > 0

    suspend fun saveSource(id: Long?, projectId: Long?, name: String, url: String?): Boolean {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return false
        val now = System.currentTimeMillis()
        return if (id == null) sourceDao.insert(SourceEntity(projectId = projectId, name = trimmed, url = url?.trim()?.ifBlank { null }, createdAt = now, updatedAt = now)) > 0
        else sourceDao.update(id, trimmed, url?.trim()?.ifBlank { null }, projectId, now) > 0
    }

    suspend fun deleteSource(id: Long): Boolean = sourceDao.delete(id) > 0

    private fun SourceEntity.toDomain() = Source(id, projectId, name, url)
}
