package app.xiguang.projects

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.Project
import app.xiguang.domain.model.Source
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProjectsUiState(val projects: List<Project> = emptyList(), val unassignedSources: List<Source> = emptyList())
class ProjectsViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = (application as XiguangApplication).projectRepository
    val uiState = combine(repo.observeProjects(), repo.observeUnassignedSources()) { projects, sources -> ProjectsUiState(projects, sources) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectsUiState())
    fun saveProject(id:Long?, name:String, description:String?)=viewModelScope.launch { repo.saveProject(id,name,description) }
    fun deleteProject(id:Long)=viewModelScope.launch { repo.deleteProject(id) }
    fun saveSource(id:Long?, projectId:Long?, name:String, url:String?)=viewModelScope.launch { repo.saveSource(id,projectId,name,url) }
    fun deleteSource(id:Long)=viewModelScope.launch { repo.deleteSource(id) }
}
@Composable fun ProjectsRoute(viewModel: ProjectsViewModel = viewModel()) { val state by viewModel.uiState.collectAsState(); ProjectsScreen(state, viewModel::saveProject, viewModel::deleteProject, viewModel::saveSource, viewModel::deleteSource) }
@Composable private fun ProjectsScreen(state:ProjectsUiState,onSaveProject:(Long?,String,String?)->Unit,onDeleteProject:(Long)->Unit,onSaveSource:(Long?,Long?,String,String?)->Unit,onDeleteSource:(Long)->Unit){
    var projectEdit by remember { mutableStateOf<Project?>(null) }; var sourceEdit by remember { mutableStateOf<Pair<Source,Long?>?>(null) }; var newProject by remember { mutableStateOf(false) }; var newSourceProjectId by remember { mutableStateOf<Long?>(null) }; var showNewSource by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(24.dp)){ Row(Modifier.fillMaxWidth()){Text(stringResource(R.string.projects_title),style=MaterialTheme.typography.displayLarge,modifier=Modifier.weight(1f));TextButton(onClick={newProject=true}){Text(stringResource(R.string.project_add))}}
        LazyColumn { items(state.projects,key=Project::id){ project-> Column(Modifier.fillMaxWidth().padding(vertical=14.dp)){Row{Text(project.name,style=MaterialTheme.typography.titleLarge,modifier=Modifier.weight(1f));TextButton(onClick={projectEdit=project}){Text(stringResource(R.string.edit_action))};TextButton(onClick=onDeleteProject.bind(project.id)){Text(stringResource(R.string.delete_action))}};project.description?.let{Text(it,color=MaterialTheme.colorScheme.onSurfaceVariant)};Row{TextButton(onClick={newSourceProjectId=project.id;showNewSource=true}){Text(stringResource(R.string.source_add))}};project.sources.forEach{source->SourceRow(source,{sourceEdit=source to project.id},{onDeleteSource(source.id)})};HorizontalDivider(color=MaterialTheme.colorScheme.outline)}}
            if(state.unassignedSources.isNotEmpty()) item{Text(stringResource(R.string.source_unassigned),style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(top=16.dp));state.unassignedSources.forEach{source->SourceRow(source,{sourceEdit=source to null},{onDeleteSource(source.id)})}}
        }
        TextButton(onClick={newSourceProjectId=null;showNewSource=true}){Text(stringResource(R.string.source_add_unassigned))}
    }
    if(newProject) ProjectDialog(null,{newProject=false}){n,d->onSaveProject(null,n,d);newProject=false}; projectEdit?.let{p->ProjectDialog(p,{projectEdit=null}){n,d->onSaveProject(p.id,n,d);projectEdit=null}}
    if(showNewSource) SourceDialog(null,newSourceProjectId,{showNewSource=false}){n,u->onSaveSource(null,newSourceProjectId,n,u);showNewSource=false}; sourceEdit?.let{(s,p)->SourceDialog(s,p,{sourceEdit=null}){n,u->onSaveSource(s.id,p,n,u);sourceEdit=null}}
}
private fun ((Long)->Unit).bind(id:Long):()->Unit = { this(id) }
@Composable private fun SourceRow(source:Source,onEdit:()->Unit,onDelete:()->Unit){Row(Modifier.fillMaxWidth().padding(start=16.dp,top=6.dp)){Text(source.name,modifier=Modifier.weight(1f));TextButton(onClick=onEdit){Text(stringResource(R.string.edit_action))};TextButton(onClick=onDelete){Text(stringResource(R.string.delete_action))}}}
@Composable private fun ProjectDialog(project:Project?,onDismiss:()->Unit,onSave:(String,String?)->Unit){var n by remember(project){mutableStateOf(project?.name.orEmpty())};var d by remember(project){mutableStateOf(project?.description.orEmpty())};AlertDialog(onDismissRequest=onDismiss,title={Text(stringResource(R.string.project_name))},text={Column{OutlinedTextField(n,{n=it},label={Text(stringResource(R.string.project_name))});OutlinedTextField(d,{d=it},label={Text(stringResource(R.string.project_description))})}},confirmButton={TextButton(onClick={onSave(n,d)}){Text(stringResource(R.string.save_action))}},dismissButton={TextButton(onClick=onDismiss){Text(stringResource(R.string.cancel_action))}})}
@Composable private fun SourceDialog(source:Source?,projectId:Long?,onDismiss:()->Unit,onSave:(String,String?)->Unit){var n by remember(source){mutableStateOf(source?.name.orEmpty())};var u by remember(source){mutableStateOf(source?.url.orEmpty())};AlertDialog(onDismissRequest=onDismiss,title={Text(stringResource(R.string.source_name))},text={Column{OutlinedTextField(n,{n=it},label={Text(stringResource(R.string.source_name))});OutlinedTextField(u,{u=it},label={Text(stringResource(R.string.source_url))})}},confirmButton={TextButton(onClick={onSave(n,u)}){Text(stringResource(R.string.save_action))}},dismissButton={TextButton(onClick=onDismiss){Text(stringResource(R.string.cancel_action))}})}
