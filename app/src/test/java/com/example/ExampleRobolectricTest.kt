package com.example

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.ads.AdConfig
import com.example.data.local.CalculationHistoryEntity
import com.example.data.local.DailyFixDatabase
import com.example.data.repository.DailyFixRepository
import com.example.domain.model.DailyTool
import com.example.domain.model.ToolCategory
import com.example.ui.DailyFixMainUiState
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.DailyFixTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: DailyFixDatabase
    private lateinit var repository: DailyFixRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, DailyFixDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = DailyFixRepository(db.dailyFixDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun verifyAppIdentityAndAdMobManifestMetadata() {
        assertEquals("DailyFix", context.getString(R.string.app_name))
        assertEquals("com.dailyfix.smarttools", BuildConfig.APPLICATION_ID)
        assertTrue(AdConfig.useTestAds)
        assertEquals(AdConfig.TEST_BANNER_AD_UNIT_ID, AdConfig.bannerAdUnitId)
        assertEquals(AdConfig.TEST_INTERSTITIAL_AD_UNIT_ID, AdConfig.interstitialAdUnitId)

        val appInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        val manifestAdMobAppId = appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
        assertEquals("ca-app-pub-3781293651763401~4637525567", manifestAdMobAppId)
    }

    @Test
    fun roomRepository_savesFavoritesRecentsAndHistoryLocally() = runTest {
        repository.toggleFavorite(DailyTool.UNIT_CONVERTER)
        val favorites = repository.favoriteToolIds.first()
        assertTrue(DailyTool.UNIT_CONVERTER.id in favorites)

        repository.saveHistory(
            tool = DailyTool.DISCOUNT_CALCULATOR,
            expressionSummary = "100.00 - 20%",
            resultSummary = "80.00"
        )
        val history: List<CalculationHistoryEntity> = repository.history.first()
        assertEquals(1, history.size)
        assertEquals("80.00", history.first().resultSummary)

        val recents = repository.recentTools.first()
        assertEquals(DailyTool.DISCOUNT_CALCULATOR, recents.first())

        repository.clearHistory()
        assertTrue(repository.history.first().isEmpty())
    }

    @Test
    fun homeScreen_rendersInDarkModeAndFiltersSearch() {
        var searchText = ""
        composeTestRule.setContent {
            DailyFixTheme(themeMode = ThemeMode.DARK) {
                HomeScreen(
                    uiState = DailyFixMainUiState(
                        searchQuery = searchText,
                        selectedCategory = ToolCategory.ALL,
                        favoriteToolIds = setOf(DailyTool.SMART_CALCULATOR.id)
                    ),
                    onSearchQueryChange = { searchText = it },
                    onCategorySelected = {},
                    onOpenTool = {},
                    onToggleFavorite = {},
                    onOpenAllTools = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_search_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("featured_tile_smart_calculator").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_search_input").performTextInput("loan")
        assertEquals("loan", searchText)
    }
}
