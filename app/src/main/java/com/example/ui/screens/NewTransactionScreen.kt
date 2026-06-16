package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTransactionScreen(
    viewModel: TransactionViewModel,
    isCloudBackupEnabled: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (Long?, String, Double, String, String, String) -> Unit,
    transactionToEdit: TransactionEntity? = null
) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    var currentEditingTransaction by remember(transactionToEdit) { mutableStateOf<TransactionEntity?>(transactionToEdit) }

    var description by remember(currentEditingTransaction) { mutableStateOf(currentEditingTransaction?.description ?: "") }
    var amountText by remember(currentEditingTransaction) { mutableStateOf(currentEditingTransaction?.amount?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    val type = currentEditingTransaction?.type ?: "OUTFLOW"
    val toggleSync = remember(currentEditingTransaction) { mutableStateOf(isCloudBackupEnabled) }
    var selectedWeek by remember(currentEditingTransaction) { mutableStateOf(currentEditingTransaction?.week ?: "1ª Semana") }

    val transactions by viewModel.allTransactions.collectAsStateWithLifecycle(emptyList())
    val existingExpenses = remember(transactions) {
        transactions.filter { it.type == "OUTFLOW" }.map { it.description }.distinct().sorted()
    }
    var expandedDescDropdown by remember { mutableStateOf(false) }

    var categoryText by remember(currentEditingTransaction) { mutableStateOf(currentEditingTransaction?.category ?: "") }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    val masterCategories by viewModel.allCategories.collectAsStateWithLifecycle(emptyList())
    val existingCategoryNames = remember(masterCategories) {
        masterCategories.filter { it.type == "OUTFLOW" }.map { it.name }.distinct().sortedBy { it.lowercase() }
    }

    var expenseToRename by remember { mutableStateOf<String?>(null) }
    var renameValue by remember { mutableStateOf("") }
    var expenseToDelete by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
    ) {
        // Title Bar Header
        TopAppBar(
            title = {
                Text(
                    text = if (currentEditingTransaction != null) "Editar Gasto" else "Novo Gasto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Primary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White.copy(alpha = 0.05f)
            ),
            modifier = Modifier.drawBehindGlassBorder()
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero info description
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(Primary.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward, // Clear visual icon for Gastos/Outflows
                            contentDescription = "Gasto",
                            tint = ErrorColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentEditingTransaction != null) "Atualize as informações do gasto registrado para manter suas contas corretas." else "Registre e salve seus gastos na lista para ter total controle financeiro sem complicações.",
                        fontSize = 14.sp,
                        color = OnSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        lineHeight = 20.sp
                    )
                }
            }

            // 1. Categoria / Nome do Gasto (linked to master categories list "Recursos & Cadastros -> Nomes de Gastos")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "CATEGORIA / NOME DO GASTO (DE RECURSOS)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedCategoryDropdown,
                        onExpandedChange = { expandedCategoryDropdown = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = categoryText,
                            onValueChange = { 
                                categoryText = it
                                expandedCategoryDropdown = true 
                            },
                            placeholder = { Text("Selecione ou digite um tipo de gasto registrado...", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("category_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        val filteredCategories = existingCategoryNames.filter { it.contains(categoryText, ignoreCase = true) }
                        
                        ExposedDropdownMenu(
                            expanded = expandedCategoryDropdown,
                            onDismissRequest = { expandedCategoryDropdown = false },
                            modifier = Modifier.background(SurfaceDark).border(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            if (filteredCategories.isEmpty() && categoryText.isBlank()) {
                                DropdownMenuItem(
                                    text = { Text("Cadastre nomes de gastos no menu de Recursos", fontSize = 12.sp, color = OnSurfaceVariant) },
                                    onClick = {}
                                )
                            } else {
                                filteredCategories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat, color = OnSurface) },
                                        onClick = {
                                            categoryText = cat
                                            expandedCategoryDropdown = false
                                        }
                                    )
                                }
                            }
                            
                            val trimmedCat = categoryText.trim()
                            if (trimmedCat.isNotEmpty() && !existingCategoryNames.any { it.equals(trimmedCat, ignoreCase = true) }) {
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = "+ Cadastrar \"$trimmedCat\" no Catálogo", 
                                            color = Primary, 
                                            fontWeight = FontWeight.Bold 
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.addCategory(trimmedCat, "OUTFLOW")
                                        expandedCategoryDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 2. Descrição Opcional (Detalhes Adicionais: p.ex. "Fornecedor TexArt", "Energia do Mês")
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "HISTÓRICO / DETALHES DA SAÍDA (OPCIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    ExposedDropdownMenuBox(
                        expanded = expandedDescDropdown,
                        onExpandedChange = { expandedDescDropdown = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { 
                                description = it
                                expandedDescDropdown = true 
                            },
                            placeholder = { Text("Ex: Fornecedor TexArt, Conserto da Galoneira, etc.", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = OnSurface,
                                unfocusedTextColor = OnSurface,
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                                focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("description_input"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        val filteredExpenses = existingExpenses.filter { it.contains(description, ignoreCase = true) }
                        if (filteredExpenses.isNotEmpty() && expandedDescDropdown) {
                            ExposedDropdownMenu(
                                expanded = expandedDescDropdown,
                                onDismissRequest = { expandedDescDropdown = false },
                                modifier = Modifier.background(SurfaceDark).border(1.dp, Color.White.copy(alpha = 0.08f))
                            ) {
                                filteredExpenses.forEach { exp ->
                                    DropdownMenuItem(
                                        text = { Text(exp, color = OnSurface) },
                                        onClick = {
                                            description = exp
                                            expandedDescDropdown = false
                                        },
                                        trailingIcon = {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        expenseToRename = exp
                                                        renameValue = exp
                                                        expandedDescDropdown = false
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Editar histórico",
                                                        tint = Primary,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        expenseToDelete = exp
                                                        expandedDescDropdown = false
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Excluir histórico",
                                                        tint = ErrorColor,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Amount Input Field
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "VALOR (R$)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        placeholder = { Text("0,00", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        prefix = { Text("R$ ", color = Primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = Color.White.copy(alpha = 0.04f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.04f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Week Selection row of Chips
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "SEMANA DO REGISTRO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val weeks = listOf("1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")
                        items(weeks, key = { it }) { w ->
                            val isSelected = selectedWeek == w
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(
                                        if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f),
                                        RoundedCornerShape(32.dp)
                                    )
                                    .clickable { selectedWeek = w }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = w,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Primary else OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }



            // Submit Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val amt = amountText.replace(',', '.').toDoubleOrNull() ?: 0.0
                        if (categoryText.isNotBlank() && amt > 0) {
                            // If description is empty, default it to the category name
                            val finalDesc = description.ifBlank { categoryText }
                            onSubmit(currentEditingTransaction?.id, finalDesc, amt, categoryText.trim(), type, selectedWeek)
                            currentEditingTransaction = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary,
                        contentColor = OnPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_transaction_button"),
                    enabled = categoryText.isNotBlank() && (amountText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Text(
                            text = if (currentEditingTransaction != null) "Atualizar Gasto" else "Salvar Gasto",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (currentEditingTransaction != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            currentEditingTransaction = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OnSurfaceVariant
                        ),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "Cancelar Edição (Novo Gasto)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Recent Expenses History List (Histórico de Gastos Salvos)
            val recentExpenses = transactions.filter { it.type == "OUTFLOW" }
                .sortedByDescending { it.timestamp }
                .take(5)

            if (recentExpenses.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "HISTÓRICO RECENTE DE GASTOS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary,
                            letterSpacing = 0.5.sp
                        )
                        
                        recentExpenses.forEachIndexed { index, exp ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentEditingTransaction = exp
                                    }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = exp.category,
                                        fontSize = 13.sp,
                                        color = OnSurface,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (exp.description.isNotBlank() && exp.description != exp.category) {
                                        Text(
                                            text = exp.description,
                                            fontSize = 12.sp,
                                            color = OnSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Text(
                                        text = "${exp.week} • ${exp.dateString}",
                                        fontSize = 10.sp,
                                        color = OnSurfaceVariant.copy(alpha = 0.7f)
                                    )
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", exp.amount),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ErrorColor
                                    )
                                    
                                    IconButton(
                                        onClick = {
                                            currentEditingTransaction = exp
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Editar",
                                            tint = Primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    IconButton(
                                        onClick = {
                                            viewModel.deleteTransaction(exp.id)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Excluir",
                                            tint = ErrorColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            
                            if (index < recentExpenses.lastIndex) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.5.dp)
                            }
                        }
                    }
                }
            }

            // Pro Tip section card at the bottom
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getGlassContainerColor()
                    ),
                    border = getGlassBorderStroke(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Os gastos salvos aqui impactam diretamente seus relatórios mensais de lucratividade do seu negócio.",
                            fontSize = 11.sp,
                            color = OnSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }

    // Dialog for renaming historical description
    expenseToRename?.let { oldDesc ->
        AlertDialog(
            onDismissRequest = { expenseToRename = null },
            title = { Text("Editar Nome do Histórico", color = Primary, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Altere o nome deste item histórico para todas as despesas salvas correspondentes:", fontSize = 13.sp, color = OnSurfaceVariant)
                    OutlinedTextField(
                        value = renameValue,
                        onValueChange = { renameValue = it },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newValue = renameValue.trim()
                        if (newValue.isNotEmpty()) {
                            viewModel.renameExpenseDescription(oldDesc, newValue)
                            if (description == oldDesc) {
                                description = newValue
                            }
                            expenseToRename = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Text("Salvar", color = OnPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToRename = null }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }

    // Dialog for deleting historical description
    expenseToDelete?.let { oldDesc ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Excluir Histórico", color = ErrorColor, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Deseja realmente apagar o histórico de \"$oldDesc\"? Isso excluirá todas as despesas lançadas anteriormente com este histórico.",
                    fontSize = 13.sp,
                    color = OnSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExpenseDescription(oldDesc)
                        if (description == oldDesc) {
                            description = ""
                        }
                        expenseToDelete = null
                        expandedDescDropdown = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancelar", color = OnSurfaceVariant)
                }
            }
        )
    }
}
