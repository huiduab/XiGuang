package app.xiguang.today

import android.app.Application
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import app.xiguang.R
import app.xiguang.XiguangApplication
import app.xiguang.domain.model.Platform
import app.xiguang.domain.model.SavedCollection
import app.xiguang.ui.theme.MineralBlue
import app.xiguang.ui.theme.OxidizedCopper
import app.xiguang.ui.theme.Plum
import app.xiguang.ui.theme.Sage
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import kotlin.math.roundToInt

data class TodayUiState(
    val unread: List<SavedCollection> = emptyList(),
    val addedToday: List<SavedCollection> = emptyList(),
    val random: SavedCollection? = null,
) {
    val todayReadCount: Int
        get() = addedToday.count(SavedCollection::isRead)

    val todayUnreadCount: Int
        get() = addedToday.size - todayReadCount

    val todayProgress: Float
        get() = if (addedToday.isEmpty()) 0f else todayReadCount.toFloat() / addedToday.size
}

class TodayViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = (application as XiguangApplication).collectionRepository

    val uiState = repository.observeCollections()
        .map { items ->
            val start = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            TodayUiState(
                unread = items.filterNot(SavedCollection::isRead),
                addedToday = items.filter { it.createdAt >= start },
                random = items.randomOrNull(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TodayUiState(),
        )
}

@Composable
fun TodayRoute(
    onOpenCollection: (Long) -> Unit,
    viewModel: TodayViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    TodayScreen(
        state = state,
        onOpenCollection = onOpenCollection,
    )
}

@Composable
internal fun TodayScreen(
    state: TodayUiState,
    onOpenCollection: (Long) -> Unit,
) {
    val todayAccent = MaterialTheme.colorScheme.secondary
    val unreadAccent = MaterialTheme.colorScheme.tertiary

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        item(key = "today-hero") {
            TodayHero(
                state = state,
                onRandomRead = state.random?.let { random ->
                    { onOpenCollection(random.id) }
                },
            )
        }

        todaySection(
            key = "today-added",
            titleRes = R.string.today_added,
            collections = state.addedToday,
            accent = todayAccent,
            onOpenCollection = onOpenCollection,
        )
        todaySection(
            key = "all-unread",
            titleRes = R.string.today_unread,
            collections = state.unread,
            accent = unreadAccent,
            onOpenCollection = onOpenCollection,
        )

        item(key = "today-completion") {
            TodayCompletion(
                hasNewContent = state.addedToday.isNotEmpty(),
                unreadCount = state.todayUnreadCount,
            )
        }
    }
}

@Composable
private fun TodayHero(
    state: TodayUiState,
    onRandomRead: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 24.dp, top = 18.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.weight(1f))
            if (onRandomRead != null) {
                TextButton(onClick = onRandomRead) {
                    Icon(
                        imageVector = Icons.Outlined.Shuffle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.today_random_read),
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp, bottom = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(62.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 22.dp),
            ) {
                Text(
                    text = stringResource(R.string.today_reading_title),
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = stringResource(
                        R.string.today_unread_count,
                        state.todayUnreadCount,
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 18.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(
                            R.string.today_progress,
                            state.todayReadCount,
                            state.addedToday.size,
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            R.string.today_progress_percent,
                            (state.todayProgress * 100).roundToInt(),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                LinearProgressIndicator(
                    progress = { state.todayProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outline,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

private fun LazyListScope.todaySection(
    key: String,
    titleRes: Int,
    collections: List<SavedCollection>,
    accent: Color,
    onOpenCollection: (Long) -> Unit,
) {
    item(key = "$key-header") {
        TodaySectionHeader(
            title = stringResource(titleRes),
            count = collections.size,
            accent = accent,
        )
    }

    if (collections.isEmpty()) {
        item(key = "$key-empty") {
            Text(
                text = stringResource(R.string.today_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 54.dp, end = 28.dp, bottom = 20.dp),
            )
        }
    } else {
        items(
            items = collections,
            key = { collection -> "$key-${collection.id}" },
        ) { collection ->
            TodayCollectionRow(
                collection = collection,
                onClick = { onOpenCollection(collection.id) },
            )
        }
    }
}

@Composable
private fun TodaySectionHeader(
    title: String,
    count: Int,
    accent: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 28.dp, end = 28.dp, top = 26.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(32.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(start = 18.dp),
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(R.string.today_section_count, count),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TodayCollectionRow(
    collection: SavedCollection,
    onClick: () -> Unit,
) {
    val accent = platformColor(collection.platform)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 54.dp, end = 28.dp, top = 15.dp, bottom = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = collection.platform.label,
            style = MaterialTheme.typography.bodyMedium,
            color = accent,
            modifier = Modifier.width(48.dp),
        )
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(accent),
        )
        Text(
            text = collection.title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (collection.isRead) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
        )
        Text(
            text = relativeTime(collection.createdAt),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp),
        )
    }
    HorizontalDivider(
        modifier = Modifier.padding(start = 122.dp, end = 28.dp),
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun TodayCompletion(
    hasNewContent: Boolean,
    unreadCount: Int,
) {
    val isComplete = hasNewContent && unreadCount == 0
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp, bottom = 34.dp),
    ) {
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 28.dp),
            color = MaterialTheme.colorScheme.outline,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isComplete) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Text(
                text = when {
                    !hasNewContent -> stringResource(R.string.today_no_new)
                    isComplete -> stringResource(R.string.today_all_read)
                    else -> stringResource(R.string.today_remaining, unreadCount)
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = if (isComplete) 12.dp else 0.dp),
            )
        }
    }
}

@Composable
private fun platformColor(platform: Platform): Color = when (platform) {
    Platform.X -> MineralBlue
    Platform.WEIBO -> OxidizedCopper
    Platform.XIAOHONGSHU -> Plum
    Platform.DOUYIN -> MaterialTheme.colorScheme.onBackground
    Platform.BILIBILI -> Plum
    Platform.ZHIHU -> MineralBlue
    Platform.YOUTUBE -> OxidizedCopper
    Platform.BLOG -> Sage
    Platform.OTHER -> MaterialTheme.colorScheme.primary
}

private fun relativeTime(timestamp: Long): String = DateUtils.getRelativeTimeSpanString(
    timestamp,
    System.currentTimeMillis(),
    DateUtils.MINUTE_IN_MILLIS,
    DateUtils.FORMAT_ABBREV_RELATIVE,
).toString()
