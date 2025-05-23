package com.example.fitbite.presentation.view.compose

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SettingsScreenFull(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dialogToShow by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Цвета из темы или по дефолту (можешь настроить под свои)
    val yellow = Color(0xFFFFFAEC)
    val orange = Color(0xFFFFF2E3)
    val green = Color(0xFFF3FAEE)
    val blue = Color(0xFFE8F6FB)

    // Для тёмной темы подложку делаем чуть темнее
    val backgroundColor = MaterialTheme.colorScheme.background

    Surface(
        color = backgroundColor,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 24.dp) // отступы от краёв
        ) {
            // Заголовок по центру
            Text(
                text = "Настройки",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 28.dp),
                textAlign = TextAlign.Center
            )

            // Переключатель темы (с иконкой слева)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(blue, RoundedCornerShape(18.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    "Темная тема",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onThemeChange
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SettingsButton(
                text = "Условия использования",
                color = yellow,
                onClick = { dialogToShow = "terms" }
            )
            SettingsButton(
                text = "Политика конфиденциальности",
                color = orange,
                onClick = { dialogToShow = "policy" }
            )
            SettingsButton(
                text = "Обратная связь",
                color = green,
                onClick = { dialogToShow = "feedback" }
            )
            SettingsButton(
                text = "Инструкция пользователя",
                color = yellow,
                onClick = { dialogToShow = "instruct" }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Кнопка "Выйти" без иконки, выделена красновато-оранжевым
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFF0EE),
                    contentColor = Color(0xFFFFC2C2)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(2.dp)
            ) {
                Text("Выйти", fontSize = 18.sp, color = Color.Black)
            }

            // Кнопка "Удалить аккаунт"
            Button(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(bottom = 10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFE6E6),
                    contentColor = Color(0xFFFFC2C2)
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = ButtonDefaults.buttonElevation(2.dp)
            ) {
                Text("Удалить аккаунт", fontSize = 18.sp, color = Color.Black)
            }
        }
        // Диалог подтверждения удаления аккаунта
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Удалить аккаунт?", color = Color.Black, style = MaterialTheme.typography.titleLarge) },
                text = { Text("Вы уверены, что хотите безвозвратно удалить свой аккаунт? Это действие нельзя отменить.", color = Color.Black) },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            // Вызов функции удаления аккаунта
                            onDeleteAccount()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC2C2)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Удалить", color = Color.Black)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeleteDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFFAEC)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Отмена", color = Color.Black)
                    }
                },
                containerColor = Color.White,
                shape = RoundedCornerShape(22.dp)
            )
        }

        // Диалоги
        when (dialogToShow) {
            "terms" -> {
                CustomAlertDialog(
                    title = "Условия использования",
                    assetFile = "terms_conditions.txt",
                    onClose = { dialogToShow = null }
                )
            }
            "policy" -> {
                CustomAlertDialog(
                    title = "Политика конфиденциальности",
                    assetFile = "privacy_policy.txt",
                    onClose = { dialogToShow = null }
                )
            }
            "instruct" -> {
                CustomAlertDialog(
                    title = "Инструкция пользователя",
                    assetFile = "privacy_policy.txt",
                    onClose = { dialogToShow = null }
                )
            }
            "feedback" -> {
                FeedbackDialog(
                    onClose = { dialogToShow = null }
                )
            }
        }
    }
}

@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    icon: Int? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // побольше отступы между кнопками
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            contentColor = Color.Black // текст всегда чёрный!
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.buttonElevation(1.dp)
    ) {
        if (icon != null) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color.Black
            )
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.Black
        )
    }
}

@Composable
fun CustomAlertDialog(
    title: String,
    assetFile: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                title,
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                color = Color.Black
            )
        },
        text = {
            Surface(
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 0.dp,
                modifier = Modifier
                    .heightIn(min = 320.dp, max = 500.dp)
                    .fillMaxWidth()
            ) {
                AndroidView(
                    factory = {
                        WebView(it).apply {
                            loadUrl("file:///android_asset/$assetFile")
                            settings.defaultFontSize = 24
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 260.dp, max = 420.dp)
                        .padding(0.dp) // Без рамки!
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFAEC),
                    contentColor = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    "Закрыть",
                    color = Color.Black, // всегда чёрный!
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                )
            }
        },
        containerColor = Color.White, // Белый фон окна
        shape = RoundedCornerShape(26.dp)
    )
}


@Composable
fun FeedbackDialog(onClose: () -> Unit) {
    val context = LocalContext.current

    // Цвета из твоей палитры
    val blue = Color(0xFFE8F6FB)
    val yellow = Color(0xFFFFFAEC)
    val accent = Color(0xFF000000)

    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text(
                "Выберите способ связи",
                style = MaterialTheme.typography.titleLarge,
                color = accent
            )
        },
        text = {
            Column {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/maniazvereva"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = blue,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        "Telegram",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:mashazvereva2003@gmail.com"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = yellow,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        "Почта",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFF2F2F2), // нейтральная кнопка "Закрыть"
                    contentColor = Color.Black
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("Закрыть", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            }
        },
        containerColor = Color.White, // фон диалога (можно сделать Color(0xFFF9FAFB))
        shape = RoundedCornerShape(22.dp)
    )
}




