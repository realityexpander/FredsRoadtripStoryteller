package presentation.app.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalResourceApi::class)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit = {}
) {
    val pages = listOf(
        "01-OnBoarding.png",
        "02-OnBoarding.png",
        "03-OnBoarding.png",
        "04-OnBoarding.png",
        "05-OnBoarding.png",
        "06-OnBoarding.png",
        "07-OnBoarding.png",
        "08-OnBoarding.png",
        "09-OnBoarding.png",
        "10-OnBoarding.png",
        "11-OnBoarding.png",
        "12-OnBoarding.png",
        "13-OnBoarding.png",
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

        Surface(
            modifier = Modifier
                .background(MaterialTheme.colors.background)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                HorizontalPager(
                    modifier = Modifier,
                    state = pagerState,
                    pageSpacing = 0.dp,
                    userScrollEnabled = true,
                    reverseLayout = false,
                    contentPadding = PaddingValues(0.dp),
                    beyondBoundsPageCount = 0,
                    pageSize = PageSize.Fill,
                    flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                    key = { index -> pages[index-1] },
                    pageContent = { index ->
                        Column(
                           modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(res = pages[index]),
                                contentDescription = null,
                                contentScale = ContentScale.FillHeight,
                                modifier = Modifier.fillMaxSize(.85f),
                                alignment = Alignment.Center,
                            )
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
                        .background(MaterialTheme.colors.background)
                        .padding(start=8.dp, end=8.dp, top=4.dp, bottom=4.dp)
                        .align(Alignment.BottomCenter)
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
                            imageVector = Icons.Default.KeyboardArrowLeft,
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
                            imageVector = Icons.Default.KeyboardArrowRight,
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
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
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
