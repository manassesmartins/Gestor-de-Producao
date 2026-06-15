package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.TransactionViewModel
import com.example.ui.AppTab
import com.example.ui.utils.rememberBitmapFromBase64

@Composable
fun ProfileSettingsPopup(
    onDismiss: () -> Unit,
    viewModel: TransactionViewModel
) {
    val context = LocalContext.current
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val brandConfig by viewModel.brandConfig.collectAsState()

    val iconsMap = mapOf(
        "CROWN" to Icons.Default.Star,
        "BAG" to Icons.Default.ShoppingCart,
        "HEART" to Icons.Default.Favorite,
        "BUILD" to Icons.Default.Build,
        "PERSON" to Icons.Default.Person
    )

    // Activity launcher for database export
    val exportDatabaseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/octet-stream")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val success = viewModel.exportDatabaseToStream(outputStream)
                    if (success) {
                        Toast.makeText(context, "Backup local (.db) exportado!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Falha ao exportar backup.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Activity launcher for database import
    val importDatabaseLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val success = viewModel.importDatabaseFromStream(inputStream)
                    if (success) {
                        Toast.makeText(context, "Banco de dados importado!", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Falha ao processar arquivo de banco de dados.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GERENCIAR EXPEDIENTE LOCAL",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary,
                        letterSpacing = 1.sp
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar",
                            tint = OnSurfaceVariant
                        )
                    }
                }

                // Brand details
                brandConfig?.let { config ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Primary.copy(alpha = 0.15f), CircleShape)
                                .border(1.5.dp, Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            val decodedLogo = rememberBitmapFromBase64(config.logoImage)
                            if (decodedLogo != null) {
                                androidx.compose.foundation.Image(
                                    bitmap = decodedLogo,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp).clip(CircleShape)
                                )
                            } else {
                                val selectedIcon = iconsMap[config.logoIcon] ?: Icons.Default.Star
                                Icon(
                                    imageVector = selectedIcon,
                                    contentDescription = null,
                                    tint = Primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Text(
                            text = config.brandName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "${config.category} • ${config.niche.ifBlank { "Sem Nicho" }}",
                            fontSize = 12.sp,
                            color = OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } ?: run {
                    Text(
                        text = "Nenhuma marca configurada",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant
                    )
                }

                // Status info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Criptografado",
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Segurança Máxima Local",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnSurface
                        )
                        Text(
                            text = "Seus registros são guardados exclusivamente no seu celular de forma offline.",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 14.sp
                        )
                    }
                }

                // Actions Card
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            try {
                                exportDatabaseLauncher.launch("ms_modaintima_database.db")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao exportar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Exportar Cópia de Segurança (.db)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                importDatabaseLauncher.launch("*/*")
                            } catch (e: Exception) {
                                Toast.makeText(context, "Erro ao importar", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Primary.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Importar / Restaurar Banco de Dados", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    TextButton(
                        onClick = {
                            viewModel.setTab(AppTab.SETTINGS)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajustes Avançados de Identidade", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
