package com.example.ui.screens.tools

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculators.CalcResult
import com.example.domain.calculators.SmartCalculatorEngine
import com.example.domain.model.DailyTool
import com.example.ui.components.ToolScaffold
import com.example.ui.theme.JetBrainsMonoFontFamily

@Composable
fun SmartCalculatorScreen(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveHistory: (String, String) -> Unit,
    onBack: () -> Unit,
    canRequestAds: Boolean,
    isSdkInitialized: Boolean
) {
    var expression by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var lastEvaluatedExpr by rememberSaveable { mutableStateOf("") }
    var justEvaluated by rememberSaveable { mutableStateOf(false) }
    var savedFlag by remember { mutableStateOf(false) }

    val livePreview: String? = remember(expression) {
        if (expression.isBlank()) return@remember null
        when (val res = SmartCalculatorEngine.evaluate(expression)) {
            is CalcResult.Success -> if (res.data != expression) res.data else null
            is CalcResult.Error -> null
        }
    }

    fun onEvaluate() {
        if (expression.isBlank()) return
        when (val res = SmartCalculatorEngine.evaluate(expression)) {
            is CalcResult.Success -> {
                lastEvaluatedExpr = expression
                expression = res.data
                errorMessage = null
                justEvaluated = true
                savedFlag = false
            }
            is CalcResult.Error -> {
                errorMessage = res.message
            }
        }
    }

    fun onKeyPress(key: String) {
        errorMessage = null
        savedFlag = false
        when (key) {
            "C" -> {
                expression = ""
                lastEvaluatedExpr = ""
                justEvaluated = false
            }
            "()" -> {
                if (justEvaluated) {
                    expression = ""
                    justEvaluated = false
                }
                expression = SmartCalculatorEngine.appendParenthesis(expression)
            }
            "%" -> {
                justEvaluated = false
                expression = SmartCalculatorEngine.appendPercent(expression)
            }
            "±" -> {
                justEvaluated = false
                expression = SmartCalculatorEngine.toggleSign(expression)
            }
            "÷", "×", "-", "+" -> {
                justEvaluated = false
                expression = SmartCalculatorEngine.appendOperator(expression, key)
            }
            "=" -> onEvaluate()
            else -> {
                if (justEvaluated) {
                    expression = if (key == ".") "0." else key
                    justEvaluated = false
                } else {
                    expression = SmartCalculatorEngine.appendDigitOrDot(expression, key)
                }
            }
        }
    }

    ToolScaffold(
        tool = DailyTool.SMART_CALCULATOR,
        isFavorite = isFavorite,
        onToggleFavorite = onToggleFavorite,
        onBack = onBack,
        canRequestAds = canRequestAds,
        isSdkInitialized = isSdkInitialized,
        scrollable = false
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize()
        ) {
            val useScrollLayout = maxHeight < 520.dp
            val keySpacing = if (maxHeight < 580.dp) 8.dp else 10.dp
            val delRowHeight = 48.dp
            val minDisplayHeight = 128.dp
            val reservedDisplayHeight = 184.dp
            val availableForKeys = (maxHeight - reservedDisplayHeight - delRowHeight - keySpacing * 7)
                .coerceAtLeast(240.dp)
            val keyHeight = if (useScrollLayout) {
                48.dp
            } else {
                (availableForKeys / 5f).coerceIn(48.dp, 62.dp)
            }

            Column(
                modifier = if (useScrollLayout) {
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                } else {
                    Modifier.fillMaxSize()
                },
                verticalArrangement = Arrangement.spacedBy(keySpacing)
            ) {
                CalculatorDisplayCard(
                    expression = expression,
                    lastEvaluatedExpr = lastEvaluatedExpr,
                    justEvaluated = justEvaluated,
                    livePreview = livePreview,
                    errorMessage = errorMessage,
                    savedFlag = savedFlag,
                    onSaveClick = {
                        val res = SmartCalculatorEngine.evaluate(expression)
                        if (res is CalcResult.Success) {
                            val exprToSave = if (justEvaluated && lastEvaluatedExpr.isNotEmpty()) {
                                lastEvaluatedExpr
                            } else {
                                expression
                            }
                            onSaveHistory(exprToSave, "= ${res.data}")
                            savedFlag = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = minDisplayHeight)
                )

                if (!useScrollLayout) {
                    Spacer(modifier = Modifier.weight(1f))
                }

                CalculatorKeypad(
                    keyHeight = keyHeight,
                    keySpacing = keySpacing,
                    onBackspace = {
                        errorMessage = null
                        justEvaluated = false
                        savedFlag = false
                        expression = SmartCalculatorEngine.backspace(expression)
                    },
                    onKeyPress = ::onKeyPress
                )
            }
        }
    }
}

@Composable
private fun CalculatorDisplayCard(
    expression: String,
    lastEvaluatedExpr: String,
    justEvaluated: Boolean,
    livePreview: String?,
    errorMessage: String?,
    savedFlag: Boolean,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 28.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lastEvaluatedExpr.isNotEmpty() && justEvaluated) lastEvaluatedExpr else "",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (expression.isNotEmpty() && errorMessage == null) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .clickable(onClick = onSaveClick)
                            .testTag("calc_save_history")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (savedFlag) Icons.Rounded.CheckCircle else Icons.Rounded.BookmarkBorder,
                                contentDescription = "Save calculation",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = if (savedFlag) "Saved" else "Save",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            val isLongExpression = expression.length > 14
            Text(
                text = expression.ifEmpty { "0" },
                style = MaterialTheme.typography.displayMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = if (isLongExpression) 24.sp else 34.sp,
                    lineHeight = if (isLongExpression) 30.sp else 40.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("calc_display")
            )

            AnimatedVisibility(visible = errorMessage != null || livePreview != null) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_error")
                    )
                } else if (livePreview != null) {
                    Text(
                        text = "= $livePreview",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily
                        ),
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calc_preview")
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorKeypad(
    keyHeight: Dp,
    keySpacing: Dp,
    onBackspace: () -> Unit,
    onKeyPress: (String) -> Unit
) {
    val rows = remember {
        listOf(
            listOf("C", "()", "%", "÷"),
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "+"),
            listOf("±", "0", ".", "=")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(keySpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                onClick = onBackspace,
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("calc_btn_backspace")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
                        contentDescription = "Delete last character",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "DEL",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(keySpacing)
            ) {
                row.forEach { key ->
                    CalculatorKeyButton(
                        label = key,
                        keyHeight = keyHeight,
                        modifier = Modifier.weight(1f),
                        onClick = { onKeyPress(key) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CalculatorKeyButton(
    label: String,
    keyHeight: Dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isEquals = label == "="
    val isOperator = label in listOf("÷", "×", "-", "+")
    val isFunction = label in listOf("C", "()", "%")

    val bgColor = when {
        isEquals -> MaterialTheme.colorScheme.primary
        isOperator -> MaterialTheme.colorScheme.primaryContainer
        isFunction -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val contentColor = when {
        isEquals -> Color.White
        isOperator -> MaterialTheme.colorScheme.onPrimaryContainer
        isFunction -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(keyHeight)
            .testTag("calc_btn_$label"),
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = if (!isEquals && !isOperator && !isFunction) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        } else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = if (label.all { it.isDigit() || it == '.' }) {
                        JetBrainsMonoFontFamily
                    } else {
                        MaterialTheme.typography.titleLarge.fontFamily
                    },
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor
            )
        }
    }
}
