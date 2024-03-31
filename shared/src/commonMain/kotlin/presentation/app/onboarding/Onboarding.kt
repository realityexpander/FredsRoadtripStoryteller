package presentation.app.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import fredsroadtripstoryteller.shared.generated.resources.Res
import fredsroadtripstoryteller.shared.generated.resources.a01onboarding
import fredsroadtripstoryteller.shared.generated.resources.a02onboarding
import fredsroadtripstoryteller.shared.generated.resources.a03onboarding
import fredsroadtripstoryteller.shared.generated.resources.a04onboarding
import fredsroadtripstoryteller.shared.generated.resources.a05onboarding
import fredsroadtripstoryteller.shared.generated.resources.a06onboarding
import fredsroadtripstoryteller.shared.generated.resources.a07onboarding
import fredsroadtripstoryteller.shared.generated.resources.a08onboarding
import fredsroadtripstoryteller.shared.generated.resources.a09onboarding
import fredsroadtripstoryteller.shared.generated.resources.a10onboarding
import fredsroadtripstoryteller.shared.generated.resources.a11onboarding
import fredsroadtripstoryteller.shared.generated.resources.a12onboarding
import fredsroadtripstoryteller.shared.generated.resources.a13onboarding
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit = {}
) {
    val pages =
        if (!LocalInspectionMode.current) listOf(
            Res.drawable.a01onboarding,
            Res.drawable.a02onboarding,
            Res.drawable.a03onboarding,
            Res.drawable.a04onboarding,
            Res.drawable.a05onboarding,
            Res.drawable.a06onboarding,
            Res.drawable.a07onboarding,
            Res.drawable.a08onboarding,
            Res.drawable.a09onboarding,
            Res.drawable.a10onboarding,
            Res.drawable.a11onboarding,
            Res.drawable.a12onboarding,
            Res.drawable.a13onboarding,
        ) else
            listOf(
                DrawableResource(""), // 3 empty pages for previews
                DrawableResource(""),
                DrawableResource("")
            )

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        // provide pageCount
        pages.size
    }
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
        onDismissRequest = {
            onDismiss()
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.BottomCenter
        ) {

            HorizontalPager(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 48.dp),
                state = pagerState,
                pageSpacing = 0.dp,
                userScrollEnabled = true,
                reverseLayout = false,
                contentPadding = PaddingValues(0.dp),
                beyondBoundsPageCount = 0,
                pageSize = PageSize.Fill,
                flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                key = { index -> pages[index].hashCode() },
                pageContent = { index ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {

                        if (!LocalInspectionMode.current) {
                            Image(
                                painter = painterResource(resource = pages[index]),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier.fillMaxSize(.95f),
                                alignment = Alignment.Center,
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize(.95f)
                                    .background(Color.Red),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Image Here", color = Color.Black)
                            }
                        }
                    }
                }
            )

            // Dots Indicator
            Box(
                modifier = Modifier
                    .wrapContentSize()
                    .offset(y = -(16).dp)
                    .fillMaxWidth(0.8f)
                    .clip(RoundedCornerShape(100))
                    .background(MaterialTheme.colors.surface)
                    .padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            ) {
                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage - 1
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Go back",
                        tint = MaterialTheme.colors.onBackground
                    )
                }

                DotsIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    totalDots = pages.size,
                    selectedIndex = pagerState.currentPage,
                    selectedColor = MaterialTheme.colors.onBackground,
                    unSelectedColor = MaterialTheme.colors.onBackground.copy(alpha = 0.25f)
                )

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                pagerState.currentPage + 1
                            )
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go forward",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            }

            // Close Button
            IconButton(
                onClick = {
                    onDismiss()
                },
                modifier = Modifier
                    .offset(y = 48.dp)  // skip top bar
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .background(
                        MaterialTheme.colors.surface.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.medium
                    )
                    .clickable { onDismiss() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    modifier = Modifier.alpha(0.8f)
                )
            }

        }
    }
}

@Composable
fun DotsIndicator(
    modifier: Modifier = Modifier,
    totalDots: Int,
    selectedIndex: Int,
    selectedColor: Color,
    unSelectedColor: Color,
) {
    LazyRow(
        modifier = modifier
            .wrapContentWidth()
            .wrapContentHeight()

    ) {

        items(totalDots) { index ->
            if (index == selectedIndex) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(selectedColor)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(unSelectedColor)
                )
            }

            if (index != totalDots - 1) {
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            }
        }
    }
}
