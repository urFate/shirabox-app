package org.shirabox.app.ui.activity.resource

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LiveTv
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.MovieCreation
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dagger.hilt.android.AndroidEntryPoint
import org.shirabox.app.R
import org.shirabox.app.ValuesHelper
import org.shirabox.app.ui.component.general.BaseCard
import org.shirabox.app.ui.component.general.ExpandableBox
import org.shirabox.app.ui.component.general.ExtendedListItem
import org.shirabox.app.ui.component.general.RatingView
import org.shirabox.app.ui.component.general.ScaredEmoticon
import org.shirabox.app.ui.theme.ShiraBoxTheme
import org.shirabox.core.model.ContentType
import org.shirabox.core.util.round
import java.io.IOException

@AndroidEntryPoint
class ResourceActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            ShiraBoxTheme(
                transparentStatusBar = true
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val activity = context as Activity?

                    var resourceId: Int = -1
                    lateinit var type: ContentType

                    try {
                        val arguments = intent.extras!!

                        resourceId = arguments.getInt("id")
                        type = arguments.getString("type").toString()
                            .let { ContentType.fromString(it) }
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        activity?.finish()
                        Toast.makeText(context, ex.localizedMessage, Toast.LENGTH_LONG).show()
                    }

                    Resource(resourceId, type, LocalContext.current)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalMaterialApi::class
)
@Composable
fun Resource(
    id: Int,
    type: ContentType,
    context: Context,
    model: ResourceViewModel = hiltViewModel(),
    colorScheme: ColorScheme = MaterialTheme.colorScheme
) {
    val activity = LocalContext.current as Activity?
    val content = model.content.value
    val relations by remember {
        derivedStateOf {
            model.relatedContents.filter { it.type == ContentType.ANIME }
        }
    }
    val isFavourite = model.isFavourite.value
    val isRefreshing = model.isRefreshing.value

    val isReady = remember(content) { content != null }
    val bottomSheetVisibilityState = remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }


    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            model.content.value?.let { model.refresh(it) }
        }
    )

    LaunchedEffect(Unit) {
        model.fetchContent(id)
        model.fetchRelated(id)
        model.clearNotifications(id)
    }

    AnimatedVisibility(
        visible = (!isReady && model.contentObservationException.value == null),
        exit = fadeOut()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                strokeCap = StrokeCap.Round
            )
        }
    }

    AnimatedVisibility(
        visible = model.contentObservationException.value != null,
        exit = fadeOut()
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            val errorText = when(model.contentObservationException.value) {
                is IOException -> stringResource(id = R.string.no_internet_connection_variant)
                else -> stringResource(id = R.string.no_contents)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ScaredEmoticon(text = errorText)
                Button(onClick = { activity?.finish() }) {
                    Text(stringResource(id = R.string.go_back))
                }
            }
        }
    }

    AnimatedVisibility(
        visible = isReady,
        enter = fadeIn()
    ) {
        content?.let {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(content.image)
                            .crossfade(true)
                            .build(),
                        modifier = Modifier
                            .padding(top = 0.dp)
                            .fillMaxWidth()
                            .height(512.dp)
                            .blur(2.dp)
                            .graphicsLayer(alpha = 0.99f)
                            .drawWithCache {
                                onDrawWithContent {
                                    drawContent()
                                    drawRect(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(
                                                    colorScheme.background.red,
                                                    colorScheme.background.green,
                                                    colorScheme.background.blue,
                                                    0.7f
                                                ), colorScheme.background
                                            )
                                        ), blendMode = BlendMode.SrcAtop
                                    )
                                }
                            },
                        contentDescription = "blank",
                        contentScale = ContentScale.FillWidth
                    )

                    TopAppBar(
                        title = { },
                        navigationIcon = {
                            IconButton(
                                onClick = { activity?.finish() },
                            ) {
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, "ArrowBack Icon")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(Color.Transparent),
                        actions = {
                            IconButton(
                                onClick = { dropdownExpanded = true },
                            ) {
                                Icon(Icons.Outlined.MoreVert, "MoreVert Icon")
                            }

                            DropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false }
                            ) {
                                TextButton(
                                    onClick = {
                                        shareVia(context, content.shikimoriID)
                                        dropdownExpanded = false
                                    },
                                    shape = RectangleShape
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            modifier = Modifier.size(24.dp),
                                            imageVector = Icons.Rounded.Share,
                                            contentDescription = "share",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = stringResource(id = R.string.share),
                                            fontSize = 16.sp,
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        },
                        scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
                    )

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(content.image)
                            .crossfade(true)
                            .build(),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .width(215.dp)
                            .height(300.dp)
                            .clip(RoundedCornerShape(32.dp)),
                        contentDescription = "blank",
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = content.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W800
                )

                Text(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    text = model.content.value?.enName.toString(),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W300
                )

                /**
                 * Buttons Row
                 */

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
                {
                    /**
                     * Favourites button
                     */

                    Button(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(55.dp)
                            .weight(weight = 1f, fill = false),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4141)),
                        onClick = {
                            model.switchFavouriteStatus(content.shikimoriID)
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding

                    ) {
                        Icon(
                            imageVector = if (!isFavourite) Icons.Outlined.FavoriteBorder else Icons.Filled.Favorite,
                            contentDescription = "Localized description",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            if (!isFavourite) stringResource(id = R.string.add_favourite) else stringResource(
                                id = R.string.remove_favourite
                            )
                        )
                    }

                    /**
                     * Play button
                     */

                    Button(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                            .height(55.dp)
                            .weight(weight = 1f, fill = false),
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.surfaceTint),
                        onClick = {
                            bottomSheetVisibilityState.value = true
                        },
                        contentPadding = ButtonDefaults.ButtonWithIconContentPadding

                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play button icon",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text(
                            stringResource(
                                id = when (type) {
                                    ContentType.ANIME -> R.string.watch
                                    else -> R.string.read
                                }
                            )
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(48.dp, 0.dp, 48.dp, 16.dp),
                    thickness = 1.dp
                )

                /**
                 * Content general information
                 */

                Column(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ResourceDataLabel(
                            icon = Icons.Outlined.MovieCreation,
                            text = "${(content.production?.uppercase() ?: stringResource(id = R.string.unknown_production))}, ${content.releaseYear}"
                        )
                        ResourceDataLabel(
                            icon = Icons.Outlined.EventAvailable,
                            text = "${
                                ValuesHelper.decodeKind(content.kind, context)
                            }, ${
                                ValuesHelper.decodeStatus(content.status, context)
                            }"
                        )
                        if (type == ContentType.ANIME) {
                            ResourceDataLabel(
                                icon = Icons.Outlined.LiveTv, text =
                                stringResource(
                                    id = R.string.resource_status,
                                    content.episodesAired ?: 0,
                                    if (content.episodes == 0) (content.episodesAired
                                        ?: 0) else content.episodes,
                                    content.episodeDuration ?: 20
                                )
                            )
                        }
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        content.genres.forEach {
                            InputChip(
                                selected = true,
                                label = {
                                    Text(
                                        text = it,
                                        fontWeight = FontWeight.W500,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 12.sp
                                    )
                                },
                                onClick = { /*TODO*/ }
                            )
                        }
                    }

                    /**
                     * Remove pseudo-tags from description
                     */

                    val cleanedDescription = remember {
                        content.description?.replace(
                            Regex("\\[.*?]"),
                            ""
                        ).toString()
                    }

                    content.description?.let {
                        if (it != "null") {
                            ExpandableBox(
                                startHeight = 128.dp
                            ) {
                                Text(
                                    text = cleanedDescription,
                                    fontWeight = FontWeight.Light,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                    thickness = 1.dp
                )

                Column(
                    modifier = Modifier.padding(16.dp, 0.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.rating),
                        fontSize = 21.sp,
                        fontWeight = FontWeight.W800
                    )

                    val rating = content.rating
                    val votes = remember(rating.scores.values::sum)

                    val scores = remember {
                        rating.scores.mapValues { (it.value.toFloat() / votes.toFloat()) }
                            .minus(0..5)
                    }
                    val avgRating = remember {
                        rating.scores.entries.sumOf {
                            (it.key * it.value).toDouble()
                        }.div(votes).round(2)
                    }

                    RatingView(
                        averageRating = avgRating,
                        votes = votes,
                        values = scores
                    )
                }

                HorizontalDivider(
                    modifier = Modifier
                        .padding(horizontal = 48.dp, vertical = 16.dp),
                    thickness = 1.dp
                )


                /**
                 * Resource relations
                 */

                if (relations.isNotEmpty()) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(16.dp, 0.dp),
                            text = stringResource(id = R.string.related),
                            fontSize = 21.sp,
                            fontWeight = FontWeight.W800
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            items(relations) {
                                ContentCard(
                                    modifier = Modifier.size(150.dp, 210.dp),
                                    title = it.name.ifBlank { it.enName },
                                    image = it.image,
                                    type = it.type
                                ) {
                                    context.startActivity(
                                        Intent(
                                            context,
                                            ResourceActivity::class.java
                                        ).apply {
                                            putExtra("id", it.shikimoriID)
                                            putExtra("type", it.type)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(56.dp))
            }

            ResourceBottomSheet(
                content = content,
                visibilityState = bottomSheetVisibilityState
            )
        }
    }

    Box(
        modifier = Modifier
            .padding(64.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState
        )
    }
}

@Composable
fun ResourceDataLabel(icon: ImageVector, text: String){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun shareVia(context: Context, shikimoriId: Int) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, "https://shikimori.one/animes/$shikimoriId")
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, null)
    context.startActivity(shareIntent)
}

@Composable
fun CommentComponent(username: String, avatar: String, timestamp: String, text: String) {
    ExtendedListItem(
        headlineContent = {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.align(Alignment.CenterStart),
                    text = username
                )
                Text(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    text = timestamp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        },
        supportingContent = { Text(text) },
        coverImage = avatar,
        clickable = false,
        headlineText = username,
        trailingIcon = null,
    )
}

