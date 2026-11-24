package com.example.glassfinance.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.sp
import com.example.glassfinance.data.preferences.SupportedBanks
import com.example.glassfinance.data.viewmodel.FinanceViewModel
import com.example.glassfinance.ui.calendar.CalendarScreen
import com.example.glassfinance.ui.components.GlassBackground
import com.example.glassfinance.ui.components.GlassNavBar
import androidx.compose.ui.graphics.Brush
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Alignment
import com.example.glassfinance.ui.components.LocalNavBarOpacity
import com.example.glassfinance.ui.components.LocalGlassOpacity
import com.example.glassfinance.ui.components.rememberPressScale
import com.example.glassfinance.ui.dashboard.DashboardScreen
import com.example.glassfinance.ui.debts.DebtsScreen
import com.example.glassfinance.ui.settings.SettingsScreen
import com.example.glassfinance.ui.statistics.StatisticsScreen
import com.example.glassfinance.ui.transactions.TransactionEditorDialog
import com.example.glassfinance.ui.transactions.TransactionsScreen
import com.example.glassfinance.ui.transactions.TransactionDetailScreen
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

private data class NavigationItem(
    val title: String,
    val icon: String
)

private val navigationItems = listOf(
    NavigationItem("Tổng quan", "⌂"),
    NavigationItem("Giao dịch", "⇄"),
    NavigationItem("Lịch", "▦"),
    NavigationItem("Thống kê", "▥")
)

/** Cài đặt không còn nằm trên thanh điều hướng; mở từ nút ⚙ cạnh nút mắt ở Tổng quan. */
private const val SETTINGS_TAB = 4

@Composable
fun GlassFinanceApp(viewModel: FinanceViewModel) {
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    val goals by viewModel.savingsGoals.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val dataMessage by viewModel.dataMessage.collectAsState()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var selectedTransactionId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val selectedTransaction = transactions.firstOrNull {
        it.id == selectedTransactionId
    }

    val detailOpen = selectedTransaction != null

    // Sổ vay / nợ mở như một màn phụ, phủ lên các tab.
    var debtsOpen by rememberSaveable { mutableStateOf(false) }
    val overlayOpen = detailOpen || debtsOpen

    // Form thêm / sửa giao dịch dùng chung cho mọi tab.
    var editorOpen by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = transactions.firstOrNull { it.id == editingId }

    val enabledBanks = SupportedBanks.all.filter { it in settings.enabledBanks }

    fun openEditor(id: Long?) {
        editingId = id
        editorOpen = true
    }

    // Back ở tab khác → quay về Tổng quan thay vì thoát app.
    BackHandler(
        enabled = overlayOpen || selectedTab != 0
    ) {
        if (detailOpen) {
            selectedTransactionId = null
        } else if (debtsOpen) {
            debtsOpen = false
        } else {
            selectedTab = 0
        }
    }

    val fabSource = remember { MutableInteractionSource() }
    val fabScale = rememberPressScale(fabSource, 0.88f)

    // Backdrop của thanh điều hướng: ghi lại cả nền lẫn nội dung màn hình (thanh nằm ngoài vùng ghi
    // nên không bị vòng lặp), nhờ đó kính khúc xạ được những gì đang hiện phía sau.
    val navBackdrop = rememberLayerBackdrop()

    CompositionLocalProvider(
        LocalGlassOpacity provides settings.glassOpacity,
        LocalNavBarOpacity provides settings.navBarOpacity
    ) {
    Box(modifier = Modifier.fillMaxSize()) {
    Box(modifier = Modifier.fillMaxSize().layerBackdrop(navBackdrop)) {
    GlassBackground {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            floatingActionButton = {
                // Nút ＋ bật/tắt bằng hiệu ứng phóng to – thu nhỏ thay vì hiện/ẩn đột ngột; nhún khi bấm.
                AnimatedVisibility(
                    visible = !overlayOpen && selectedTab <= 1,
                    enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(tween(150)),
                    exit = scaleOut(tween(140)) + fadeOut(tween(140))
                ) {
                    FloatingActionButton(
                        onClick = { openEditor(null) },
                        modifier = Modifier
                            // Nâng nút ＋ lên khỏi thanh điều hướng đang nổi.
                            .padding(bottom = 84.dp)
                            .graphicsLayer {
                                scaleX = fabScale.value
                                scaleY = fabScale.value
                            },
                        interactionSource = fabSource,
                        shape = RoundedCornerShape(20.dp),
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("＋", fontSize = 24.sp)
                    }
                }
            },
            // Không có bottomBar: thanh điều hướng thật lơ lửng phía ngoài Scaffold, nội dung
            // chạy xuống phía dưới thanh (các màn hình đã chừa đệm đáy để cuộn hết không bị che).
            bottomBar = {}
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (selectedTransaction != null) {
                    TransactionDetailScreen(
                        transaction = selectedTransaction,
                        hideAmounts = settings.hideBalance,
                        onBack = {
                            selectedTransactionId = null
                        },
                        onEdit = { openEditor(selectedTransaction.id) }
                    )
                } else if (debtsOpen) {
                    DebtsScreen(
                        debts = debts,
                        hideAmounts = settings.hideBalance,
                        onBack = { debtsOpen = false },
                        onAddDebt = viewModel::addDebt,
                        onPay = viewModel::payDebt,
                        onDeleteDebt = viewModel::deleteDebt,
                        onDeletePayment = viewModel::deleteDebtPayment
                    )
                } else {
                    // Chuyển tab: trượt nhẹ theo hướng tab mới + mờ dần.
                    AnimatedContent(
                        targetState = selectedTab,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            val dir = if (targetState > initialState) 1 else -1
                            (fadeIn(tween(220, delayMillis = 60)) +
                                slideInHorizontally(tween(320, easing = FastOutSlowInEasing)) { dir * it / 6 }) togetherWith
                                (fadeOut(tween(140)) +
                                    slideOutHorizontally(tween(320, easing = FastOutSlowInEasing)) { -dir * it / 6 })
                        },
                        label = "tab"
                    ) { tab ->
                        when (tab) {
                            0 -> DashboardScreen(
                                transactions = transactions,
                                goals = goals,
                                hideBalance = settings.hideBalance,
                                onToggleHideBalance = viewModel::toggleHideBalance,
                                onOpenTransaction = {
                                    selectedTransactionId = it.id
                                },
                                onSeeAllTransactions = { selectedTab = 1 },
                                onAddGoal = viewModel::addSavingsGoal,
                                onUpdateGoal = viewModel::updateSavingsGoal,
                                onDeleteGoal = viewModel::deleteSavingsGoal,
                                debts = debts,
                                onOpenDebts = { debtsOpen = true },
                                onOpenSettings = { selectedTab = SETTINGS_TAB }
                            )

                            1 -> TransactionsScreen(
                                transactions = transactions,
                                hideAmounts = settings.hideBalance,
                                onOpenTransaction = {
                                    selectedTransactionId = it.id
                                }
                            )

                            2 -> CalendarScreen(
                                transactions = transactions,
                                hideAmounts = settings.hideBalance,
                                onOpenTransaction = { selectedTransactionId = it.id }
                            )

                            3 -> StatisticsScreen(
                                transactions = transactions,
                                budgets = budgets,
                                hideAmounts = settings.hideBalance, 
                                onSetBudget = viewModel::setBudget,
                                onDeleteBudget = viewModel::deleteBudget,
                                onOpenTransaction = { selectedTransactionId = it.id }
                            )

                            else -> SettingsScreen(
                                settings = settings,
                                transactions = transactions,
                                debtCount = debts.size,
                                onThemeModeChange = viewModel::setThemeMode,
                                onHideBalanceChange = viewModel::setHideBalance,
                                onBankEnabledChange = viewModel::setBankEnabled,
                                onExport = viewModel::export,
                                onImport = viewModel::importBackup,
                                dataMessage = dataMessage,
                                onDismissMessage = viewModel::clearDataMessage,
                                onAddSampleData = viewModel::addSampleData,
                                onClearAll = viewModel::clearAllData,
                                onGlassOpacityChange = viewModel::setGlassOpacity,
                                onNavBarOpacityChange = viewModel::setNavBarOpacity
                            )
                        }
                    }
                }
            }
        }
    }
    }

    AnimatedVisibility(
        visible = !overlayOpen,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(spring(dampingRatio = 0.8f)) { it } + fadeIn(tween(160)),
        exit = slideOutVertically(tween(180)) { it } + fadeOut(tween(120))
    ) {
        GlassNavBar(
            items = navigationItems.map { it.icon to it.title },
            selected = selectedTab,
            onSelect = { selectedTab = it },
            backdrop = navBackdrop
        )
    }
    }
    }

    // Chờ dữ liệu nạp xong (sau khi xoay màn hình) rồi mới mở form sửa,
    // tránh form mở ra với dữ liệu trống.
    if (editorOpen && (editingId == null || editing != null)) {
        TransactionEditorDialog(
            initial = editing,
            banks = enabledBanks,
            onDismiss = {
                editorOpen = false
                editingId = null
            },
            onSave = viewModel::saveTransaction,
            onDelete = viewModel::deleteTransaction
        )
    }
}
