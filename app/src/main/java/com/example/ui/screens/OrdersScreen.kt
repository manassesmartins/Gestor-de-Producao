package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import com.example.data.*
import com.example.ui.theme.*
import java.util.Locale
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(viewModel: TransactionViewModel) {
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    val Secondary = MaterialTheme.colorScheme.secondary
    val OnSecondary = MaterialTheme.colorScheme.onSecondary
    val Tertiary = MaterialTheme.colorScheme.tertiary
    val OnTertiary = MaterialTheme.colorScheme.onTertiary
    val OnSurface = MaterialTheme.colorScheme.onSurface
    val OnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val SurfaceContainer = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceContainerHigh = MaterialTheme.colorScheme.surfaceVariant
    val SurfaceDark = MaterialTheme.colorScheme.background
    val ErrorColor = MaterialTheme.colorScheme.error

    val context = LocalContext.current
    val orders by viewModel.allOrders.collectAsStateWithLifecycle(emptyList())
    val closedMonths by viewModel.allClosedMonths.collectAsStateWithLifecycle(emptyList())
    val closedMonthsSet = remember(closedMonths) { closedMonths.map { it.monthYear }.toSet() }
    val brandConfig by viewModel.brandConfig.collectAsStateWithLifecycle()
    val brandName = brandConfig?.brandName ?: "Gestor de Produção"
    val existingClients = remember(orders) { orders.map { it.clientName }.distinct().sorted() }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    var selectedOrderWeek by remember { mutableStateOf("Tudo") }
    var showAddDialog by remember { mutableStateOf(false) }
    var orderToEdit by remember { mutableStateOf<com.example.data.OrderEntity?>(null) }
    var orderForInvoice by remember { mutableStateOf<com.example.data.OrderEntity?>(null) }
    
    var filterUrgentOnly by remember { mutableStateOf(false) }

    val overdueOrdersCount = remember(orders) {
        orders.count { order ->
            if (order.status == "Concluído") false
            else {
                val info = getDeadlineInfo(order.timestamp, order.status)
                info.status == DeadlineStatus.OVERDUE
            }
        }
    }

    val nearTodayOrdersCount = remember(orders) {
        orders.count { order ->
            if (order.status == "Concluído") false
            else {
                val info = getDeadlineInfo(order.timestamp, order.status)
                info.status == DeadlineStatus.TODAY || info.status == DeadlineStatus.NEAR
            }
        }
    }

    var showDatePickerDialog by remember { mutableStateOf(false) }
    var selectedFilterDateMillis by remember { mutableStateOf<Long?>(null) }

    val currentMonthYear = remember { SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(Date()) }
    val activeMonth = remember(closedMonthsSet) {
        var active = currentMonthYear
        val cal = java.util.Calendar.getInstance()
        while (closedMonthsSet.contains(active)) {
            try {
                cal.time = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(active) ?: Date()
                cal.add(java.util.Calendar.MONTH, 1)
                active = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(cal.time)
            } catch (e: Exception) {
                break
            }
        }
        active
    }

    val monthYearFormatter = remember { SimpleDateFormat("MM/yyyy", Locale("pt", "BR")) }
    val availableMonths = remember(orders, closedMonthsSet, activeMonth) {
        val formats = orders.map { 
            SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(Date(it.timestamp)) 
        }
        val set = (formats + currentMonthYear + activeMonth).distinct()
        
        // Filter out future months unless there is at least one scheduled order in them or it is the activeMonth
        val currentMonthDate = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(currentMonthYear) ?: Date()
        val filteredSet = set.filter { m ->
            val mDate = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(m) ?: Date()
            if (closedMonthsSet.contains(m)) {
                // If the month is closed, it shouldn't be visible in active tabs
                false
            } else if (m == currentMonthYear) {
                true
            } else if (mDate.after(currentMonthDate) && m != activeMonth) {
                orders.any { 
                    SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(Date(it.timestamp)) == m 
                }
            } else {
                true
            }
        }

        // Sort chronologically
        filteredSet.sortedWith { m1, m2 ->
            val d1 = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(m1) ?: Date()
            val d2 = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(m2) ?: Date()
            d1.compareTo(d2)
        }
    }
    
    var selectedMonthTab by remember(availableMonths) { 
        mutableStateOf(
            if (availableMonths.contains(activeMonth)) activeMonth
            else if (availableMonths.contains(currentMonthYear)) currentMonthYear
            else availableMonths.firstOrNull() ?: currentMonthYear
        ) 
    }

    LaunchedEffect(selectedMonthTab) {
        viewModel.setSelectedMonthYear(selectedMonthTab)
    }

    LaunchedEffect(selectedFilterDateMillis) {
        selectedFilterDateMillis?.let { millis ->
            val monthStr = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(Date(millis))
            if (availableMonths.contains(monthStr)) {
                selectedMonthTab = monthStr
            }
        }
    }

    val filteredOrders = remember(selectedMonthTab, selectedOrderWeek, selectedFilterDateMillis, filterUrgentOnly, orders) {
        val baseList = if (filterUrgentOnly) {
            orders
        } else {
            orders.filter { 
                SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).format(Date(it.timestamp)) == selectedMonthTab 
            }
        }
        
        val byWeek = if (selectedOrderWeek == "Tudo" || filterUrgentOnly) baseList else baseList.filter { o -> o.week == selectedOrderWeek }
        val byDate = if (selectedFilterDateMillis != null && !filterUrgentOnly) {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = selectedFilterDateMillis!!
            val d1 = cal.get(java.util.Calendar.DAY_OF_YEAR)
            val y1 = cal.get(java.util.Calendar.YEAR)
            
            byWeek.filter { o ->
                val c2 = java.util.Calendar.getInstance()
                c2.timeInMillis = o.timestamp
                c2.get(java.util.Calendar.DAY_OF_YEAR) == d1 && c2.get(java.util.Calendar.YEAR) == y1
            }
        } else {
            byWeek
        }

        if (filterUrgentOnly) {
            byDate.filter { o ->
                if (o.status == "Concluído") false
                else {
                    val info = getDeadlineInfo(o.timestamp, o.status)
                    info.status == DeadlineStatus.OVERDUE || info.status == DeadlineStatus.TODAY || info.status == DeadlineStatus.NEAR
                }
            }
        } else {
            byDate
        }
    }

    val groupedOrders = remember(filteredOrders) {
        filteredOrders.groupBy { Pair(it.clientName.trim().lowercase(Locale.getDefault()), it.week) }.values.toList()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp)
        ) {
            // Screen Title header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Agendamento de Pedidos",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Text(
                            text = "Registre e organize as encomendas da semana",
                            fontSize = 13.sp,
                            color = OnSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = { showDatePickerDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selectedFilterDateMillis != null) Primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Calendário",
                            tint = if (selectedFilterDateMillis != null) Primary else OnSurfaceVariant
                        )
                    }
                }
            }

            if (overdueOrdersCount > 0 || nearTodayOrdersCount > 0) {
                item {
                    val cardBg = if (overdueOrdersCount > 0) {
                        ErrorColor.copy(alpha = 0.08f)
                    } else {
                        Tertiary.copy(alpha = 0.08f)
                    }
                    val borderColor = if (overdueOrdersCount > 0) {
                        ErrorColor.copy(alpha = 0.3f)
                    } else {
                        Tertiary.copy(alpha = 0.3f)
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { filterUrgentOnly = !filterUrgentOnly },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(if (overdueOrdersCount > 0) ErrorColor.copy(alpha = 0.2f) else Tertiary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (overdueOrdersCount > 0) Icons.Default.Warning else Icons.Default.Notifications,
                                        contentDescription = "Alerta",
                                        tint = if (overdueOrdersCount > 0) ErrorColor else Tertiary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Column {
                                    Text(
                                        text = "Painel de Lembretes",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val alertText = buildString {
                                        if (overdueOrdersCount > 0) {
                                            append("$overdueOrdersCount encomenda(s) atrasada(s)")
                                        }
                                        if (nearTodayOrdersCount > 0) {
                                            if (this.isNotEmpty()) append(" e ")
                                            append("$nearTodayOrdersCount vencendo logo")
                                        }
                                    }
                                    Text(
                                        text = alertText,
                                        fontSize = 12.sp,
                                        color = OnSurfaceVariant
                                    )
                                }
                            }
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (filterUrgentOnly) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Primary.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Filtrado",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Primary
                                        )
                                    }
                                    IconButton(
                                        onClick = { filterUrgentOnly = false },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Limpar Filtro",
                                            tint = OnSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Ver urgentes",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Primary
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = "Filtrar",
                                        tint = Primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Dynamic Monthly Tabs
            item {
                if (availableMonths.size > 1) {
                    ScrollableTabRow(
                        selectedTabIndex = availableMonths.indexOf(selectedMonthTab).coerceIn(0, availableMonths.size - 1),
                        containerColor = Color.Transparent,
                        contentColor = Primary,
                        edgePadding = 0.dp,
                        divider = {},
                        indicator = { tabPositions ->
                            if (tabPositions.isNotEmpty()) {
                                val index = availableMonths.indexOf(selectedMonthTab).coerceIn(0, availableMonths.size - 1)
                                TabRowDefaults.SecondaryIndicator(
                                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                    color = Primary
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("months_tab_row")
                    ) {
                        availableMonths.forEach { m ->
                            val isSelected = selectedMonthTab == m
                            val parsedDate = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(m) ?: Date()
                            val monthLabel = SimpleDateFormat("MMM/yy", Locale("pt", "BR")).format(parsedDate)
                                .replace(".", "")
                                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
                            
                            Tab(
                                selected = isSelected,
                                onClick = { selectedMonthTab = m },
                                modifier = Modifier.testTag("tab_$m")
                            ) {
                                Text(
                                    text = monthLabel,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Primary else OnSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    val parsedDate = SimpleDateFormat("MM/yyyy", Locale("pt", "BR")).parse(selectedMonthTab) ?: Date()
                    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(parsedDate)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("pt", "BR")) else it.toString() }
                    Text(
                        text = "Exibindo encomendas de: $monthLabel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Weeks Filter chip bar
            item {
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    val weeks = listOf("Tudo", "1ª Semana", "2ª Semana", "3ª Semana", "4ª Semana", "5ª Semana")
                    items(weeks, key = { it }) { w ->
                        val isSelected = selectedOrderWeek == w
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(32.dp))
                                .background(if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                .border(1.dp, if (isSelected) Primary.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.1f), RoundedCornerShape(32.dp))
                                .clickable { selectedOrderWeek = w }
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

            // Orders summary
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total de Encomendas: ${filteredOrders.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OnSurfaceVariant
                    )
                    Text(
                        text = String.format(Locale("pt", "BR"), "Valor Acumulado: R$ %,.2f", filteredOrders.sumOf { it.totalValue }),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary
                    )
                }
            }

            // Order Rows
            if (groupedOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum agendamento registrado para esta semana.",
                            color = OnSurfaceVariant,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(groupedOrders, key = { it.first().id }) { group ->
                    val firstOrder = group.first()
                    val totalQty = group.sumOf { it.quantity }
                    val totalValue = group.sumOf { it.totalValue }

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = getGlassContainerColor()),
                        border = getGlassBorderStroke(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = firstOrder.clientName.uppercase(),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OnSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (group.size > 1) {
                                        Text(
                                            text = "${group.size} Itens neste pedido",
                                            fontSize = 13.sp,
                                            color = OnSurfaceVariant
                                        )
                                    }
                                    
                                    val activeOrders = group.filter { it.status != "Concluído" }
                                    val earliestActiveDue = activeOrders.minOfOrNull { it.timestamp }
                                    if (earliestActiveDue != null) {
                                        val dlInfo = getDeadlineInfo(earliestActiveDue, "Pendente")
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            val badgeColor = when (dlInfo.status) {
                                                DeadlineStatus.OVERDUE -> ErrorColor
                                                DeadlineStatus.TODAY -> Tertiary
                                                DeadlineStatus.NEAR -> Primary
                                                else -> OnSurfaceVariant
                                            }
                                            Icon(
                                                imageVector = if (dlInfo.status == DeadlineStatus.OVERDUE) Icons.Default.Warning else Icons.Default.Notifications,
                                                contentDescription = "Vencimento",
                                                tint = badgeColor,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = dlInfo.label,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = badgeColor
                                            )
                                        }
                                    } else if (group.isNotEmpty()) {
                                        Text(
                                            text = "Pedido Concluído",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Tertiary,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Primary.copy(alpha = 0.12f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = firstOrder.week,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // List all sub-items in the group
                            group.forEach { order ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("${order.pantyType} - TAM / ${order.pantySize}", fontSize = 13.sp, color = OnSurface, fontWeight = FontWeight.Bold)
                                        Text("Qtd: ${order.quantity} | R$ ${order.pantyValue} un.", fontSize = 11.sp, color = OnSurfaceVariant)
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 4.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (order.status == "Concluído") Tertiary.copy(alpha=0.2f) else ErrorColor.copy(alpha=0.2f))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text(order.status, fontSize = 9.sp, color = if (order.status == "Concluído") Tertiary else ErrorColor, fontWeight = FontWeight.SemiBold)
                                            }
                                            
                                            val sDateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
                                            if (order.status != "Concluído") {
                                                val itemDl = getDeadlineInfo(order.timestamp, order.status)
                                                val badgeColor = when (itemDl.status) {
                                                    DeadlineStatus.OVERDUE -> ErrorColor
                                                    DeadlineStatus.TODAY -> Tertiary
                                                    DeadlineStatus.NEAR -> Primary
                                                    else -> OnSurfaceVariant
                                                }
                                                Text(
                                                    text = "• ${itemDl.label}",
                                                    fontSize = 11.sp,
                                                    color = badgeColor,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            } else {
                                                Text(
                                                    text = "• Prazo: ${sDateFormatter.format(Date(order.timestamp))}",
                                                    fontSize = 11.sp,
                                                    color = OnSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = String.format(Locale("pt", "BR"), "R$ %,.2f", order.totalValue),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Tertiary,
                                            modifier = Modifier.padding(bottom = 6.dp)
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            IconButton(
                                                onClick = { orderToEdit = order },
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
                                                onClick = { viewModel.deleteOrder(order.id) },
                                                modifier = Modifier.size(32.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Apagar",
                                                    tint = ErrorColor,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                if (order != group.last()) {
                                    Divider(color = Color.White.copy(alpha = 0.05f), modifier = Modifier.padding(vertical = 4.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Divider(color = Color.White.copy(alpha = 0.05f))
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("QUANTIDADE TOTAL", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text("$totalQty unidades", fontSize = 14.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("TOTAL GERAL DO PEDIDO", fontSize = 10.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalValue),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Tertiary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // PDF slip invoice icon (we'll just use the first order as the lead for the PDF, ideally we'd pass the whole group, but to keep it simple, we pass the first order - the user requested merging in UI)
                                OutlinedButton(
                                    onClick = { orderForInvoice = firstOrder },
                                    border = BorderStroke(1.dp, Tertiary.copy(alpha = 0.3f)),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Tertiary),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Comanda Principal (PDF)", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button for Adding order
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Primary,
            contentColor = OnPrimary,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .testTag("add_order_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Adicionar Pedido",
                modifier = Modifier.size(28.dp)
            )
        }
    }

    // Modal dialog overlays
    if (showAddDialog) {
        OrderAddEditDialog(
            orders = orders,
            viewModel = viewModel,
            onDismiss = { showAddDialog = false },
            onSave = { name, model, size, qty, valUnit, week, area, status, timestamp ->
                viewModel.addOrder(name, model, size, qty, valUnit, week, area, status, timestamp)
                showAddDialog = false
                Toast.makeText(context, "Pedido agendado com sucesso!", Toast.LENGTH_SHORT).show()
            },
            defaultMonthYear = selectedMonthTab
        )
    }

    orderToEdit?.let { order ->
        OrderAddEditDialog(
            order = order,
            orders = orders,
            viewModel = viewModel,
            onDismiss = { orderToEdit = null },
            onSave = { name, model, size, qty, valUnit, week, area, status, timestamp ->
                viewModel.editOrder(order.id, name, model, size, qty, valUnit, week, area, status, timestamp)
                orderToEdit = null
                Toast.makeText(context, "Pedido atualizado!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    orderForInvoice?.let { order ->
        OrderInvoiceDialog(
            order = order,
            allOrders = orders,
            brandName = brandName,
            onDismiss = { orderForInvoice = null }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    if (showDatePickerDialog) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedFilterDateMillis ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePickerDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedFilterDateMillis = datePickerState.selectedDateMillis
                    showDatePickerDialog = false
                }) {
                    Text("Filtrar", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    selectedFilterDateMillis = null 
                    showDatePickerDialog = false 
                }) { 
                    Text("Limpar", color = ErrorColor) 
                }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceContainerHigh)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAddEditDialog(
    order: com.example.data.OrderEntity? = null,
    orders: List<com.example.data.OrderEntity>,
    viewModel: com.example.ui.TransactionViewModel,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Double, String, String, String, Long) -> Unit,
    defaultMonthYear: String = ""
) {
    val productModels by viewModel.allProductModels.collectAsStateWithLifecycle()
    val masterClients by viewModel.allClients.collectAsStateWithLifecycle()

    val existingClients = remember(orders, masterClients) {
        (masterClients.map { it.name } + orders.map { it.clientName }).distinct().sortedBy { it.lowercase() }
    }

    val existingModels = remember(orders, productModels) {
        (productModels.map { it.name } + orders.map { it.pantyType }).distinct().sortedBy { it.lowercase() }
    }

    var name by remember { mutableStateOf(order?.clientName ?: "") }
    var expandedNameDropdown by remember { mutableStateOf(false) }

    var model by remember { mutableStateOf(order?.pantyType ?: "") }
    var expandedModelDropdown by remember { mutableStateOf(false) }
    var size by remember { mutableStateOf(order?.pantySize ?: "M") }
    var qtyText by remember { mutableStateOf(order?.quantity?.toString() ?: "100") }
    var priceText by remember { mutableStateOf(order?.pantyValue?.toString() ?: "1.15") }
    
    // Convert current Order timestamp to initial date or use current time
    var selectedTimeMillis by remember { 
        mutableStateOf(
            order?.timestamp ?: System.currentTimeMillis()
        ) 
    }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Automatically calculate week string based on time
    val selectedWeek = remember(selectedTimeMillis) {
        val cal = java.util.Calendar.getInstance(Locale("pt", "BR"))
        cal.timeInMillis = selectedTimeMillis
        val weekOfMonth = cal.get(java.util.Calendar.WEEK_OF_MONTH)
        "${weekOfMonth}ª Semana"
    }

    var area by remember { mutableStateOf(order?.businessArea ?: "Geral") }
    var status by remember { mutableStateOf(order?.status ?: "Pendente") }
    
    val areaOptions = listOf("Costura", "Corte", "Bordado", "Embalagem", "Revisão", "Geral")
    val statusOptions = listOf("Pendente", "Em Andamento", "Concluído", "Atrasado")
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")) }

    val totalValue = (qtyText.toIntOrNull() ?: 0) * (priceText.replace(',', '.').toDoubleOrNull() ?: 0.0)

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedTimeMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedUtcMillis ->
                        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        utcCal.timeInMillis = selectedUtcMillis
                        val year = utcCal.get(java.util.Calendar.YEAR)
                        val month = utcCal.get(java.util.Calendar.MONTH)
                        val day = utcCal.get(java.util.Calendar.DAY_OF_MONTH)
                        
                        val localCal = java.util.Calendar.getInstance()
                        localCal.set(year, month, day, 12, 0, 0)
                        selectedTimeMillis = localCal.timeInMillis
                    }
                    showDatePicker = false
                }) {
                    Text("OK", color = Primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar", color = OnSurfaceVariant) }
            },
            colors = DatePickerDefaults.colors(containerColor = SurfaceContainerHigh)
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (order == null) "Agendar Pedido" else "Editar Pedido",
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        },
        containerColor = SurfaceContainerHigh,
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            ) {
                ExposedDropdownMenuBox(
                    expanded = expandedNameDropdown,
                    onExpandedChange = { expandedNameDropdown = it },
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it ; expandedNameDropdown = true },
                        label = { Text("Nome do Cliente", color = OnSurfaceVariant) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedNameDropdown,
                        onDismissRequest = { expandedNameDropdown = false },
                        modifier = Modifier.background(SurfaceContainerHigh).border(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        val filteredClients = existingClients.filter { it.contains(name, ignoreCase=true) }
                        filteredClients.forEach { client ->
                            DropdownMenuItem(
                                text = { Text(client, color = OnSurface) },
                                onClick = {
                                    name = client
                                    expandedNameDropdown = false
                                }
                            )
                        }

                        val trimmedName = name.trim()
                        if (trimmedName.isNotEmpty() && !masterClients.any { it.name.trim().equals(trimmedName, ignoreCase = true) }) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "+ Cadastrar \"$trimmedName\" como Cliente", 
                                        color = Primary, 
                                        fontWeight = FontWeight.Bold 
                                    ) 
                                },
                                onClick = {
                                    viewModel.addClient(trimmedName)
                                    expandedNameDropdown = false
                                }
                            )
                        }
                    }
                }

                // Historical orders preview
                val clientHistory = remember(name, orders) {
                    val matchingClientName = name.trim().lowercase(Locale.getDefault())
                    orders.filter { it.clientName.trim().lowercase(Locale.getDefault()) == matchingClientName }
                        .sortedByDescending { it.timestamp }
                        .take(3)
                }
                
                if (clientHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Histórico Recente do Cliente", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.Bold)
                        clientHistory.forEach { histOrder ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    // Autofill this historical order details
                                    model = histOrder.pantyType
                                    size = histOrder.pantySize
                                    qtyText = histOrder.quantity.toString()
                                    priceText = histOrder.pantyValue.toString()
                                },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${histOrder.pantyType} - TAM / ${histOrder.pantySize}", fontSize = 12.sp, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                    Text("${histOrder.week} - Qtd: ${histOrder.quantity}", fontSize = 10.sp, color = OnSurfaceVariant)
                                }
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Preencher dados", modifier = Modifier.size(16.dp), tint = Primary)
                            }
                            if (histOrder != clientHistory.last()) {
                                Divider(color = Color.White.copy(alpha = 0.05f))
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expandedModelDropdown,
                    onExpandedChange = { expandedModelDropdown = it },
                ) {
                    OutlinedTextField(
                        value = model,
                        onValueChange = { model = it ; expandedModelDropdown = true },
                        label = { Text("Tipo/Modelo de Calcinha", color = OnSurfaceVariant) },
                        placeholder = { Text("Ex: Cotton Summerplex", color = OnSurfaceVariant.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedModelDropdown,
                        onDismissRequest = { expandedModelDropdown = false },
                        modifier = Modifier.background(SurfaceContainerHigh).border(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        val filteredModels = existingModels.filter { it.contains(model, ignoreCase=true) }
                        filteredModels.forEach { itemModel ->
                            DropdownMenuItem(
                                text = { Text(itemModel, color = OnSurface) },
                                onClick = {
                                    model = itemModel
                                    expandedModelDropdown = false
                                }
                            )
                        }

                        val trimmedModel = model.trim()
                        if (trimmedModel.isNotEmpty() && !productModels.any { it.name.trim().equals(trimmedModel, ignoreCase = true) }) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        text = "+ Cadastrar Modelo \"$trimmedModel\"", 
                                        color = Primary, 
                                        fontWeight = FontWeight.Bold 
                                    ) 
                                },
                                onClick = {
                                    viewModel.addProductModel(trimmedModel)
                                    expandedModelDropdown = false
                                }
                            )
                        }
                    }
                }

                // Panty size selection chips
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Tamanho da Calcinha", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val sizes = listOf("P", "M", "G", "GG", "U")
                        sizes.forEach { s ->
                            val isSelected = size == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                                    .border(1.dp, if (isSelected) Primary else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .clickable { size = s }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(s, color = if (isSelected) Primary else OnSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Qtd", color = OnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Valor Unit.", color = OnSurfaceVariant) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = OnSurface,
                            unfocusedTextColor = OnSurface,
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                        ),
                        prefix = { Text("R$", color = Primary) },
                        modifier = Modifier.weight(1.3f)
                    )
                }

                // Scheduled Date & Auto-calculated Week
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Data Agendada", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Color.White.copy(0.05f)).clickable { showDatePicker = true }.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(dateFormatter.format(Date(selectedTimeMillis)), color = OnSurface, fontSize = 14.sp)
                        Icon(imageVector = Icons.Default.DateRange, contentDescription = "Selecionar Data", tint = Primary)
                    }
                    Text("Será alocado na: $selectedWeek", fontSize = 11.sp, color = Primary, fontWeight = FontWeight.SemiBold)
                }

                // Area and Status Selectors

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Status da Produção", fontSize = 11.sp, color = OnSurfaceVariant, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(statusOptions, key = { it }) { s ->
                            val isSelected = status == s
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) Tertiary.copy(alpha = 0.2f) else Color.Transparent)
                                    .border(1.dp, if (isSelected) Tertiary else Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .clickable { status = s }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(s, color = if (isSelected) Tertiary else OnSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("TOTAL PREVISTO:", fontSize = 12.sp, color = OnSurfaceVariant)
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalValue),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Tertiary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = qtyText.toIntOrNull() ?: 0
                    val valUnit = priceText.replace(',', '.').toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && model.isNotBlank() && qty > 0 && valUnit > 0) {
                        onSave(name, model, size, qty, valUnit, selectedWeek, area, status, selectedTimeMillis)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
                enabled = name.isNotBlank() && model.isNotBlank() && (qtyText.toIntOrNull() ?: 0) > 0 && (priceText.replace(',', '.').toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = OnSurfaceVariant)
            }
        }
    )
}

@Composable
fun OrderInvoiceDialog(
    order: com.example.data.OrderEntity,
    allOrders: List<com.example.data.OrderEntity>,
    brandName: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")) }
    val Primary = MaterialTheme.colorScheme.primary
    val OnPrimary = MaterialTheme.colorScheme.onPrimary
    
    val matchingOrders = remember(order, allOrders) {
        allOrders.filter { 
            it.clientName.trim().equals(order.clientName.trim(), ignoreCase = true) && 
            it.week == order.week 
        }
    }
    
    val totalToPay = remember(matchingOrders) {
        matchingOrders.sumOf { it.totalValue }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Comanda - $brandName", color = Tertiary, fontWeight = FontWeight.Bold)
        },
        containerColor = Color.White, // Paper White look!
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(Color.White),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = brandName.uppercase(Locale.getDefault()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "DOCUMENTO DE FECHAMENTO SEMANAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "--------------------------------------------------------",
                        color = Color.Black,
                        fontSize = 10.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("CLIENTE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(order.clientName.uppercase(Locale.getDefault()), fontSize = 11.sp, color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("SEMANA DO REGISTRO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Text(order.week, fontSize = 11.sp, color = Color.Black)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DATA DE EMISSÃO:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    val formattedDate = dateFormatter.format(Date(order.timestamp))
                    Text(formattedDate, fontSize = 11.sp, color = Color.Black)
                }

                Text(
                    text = "- - - - - - - - - - - - - - - - - - - - - - - - - - - - - -",
                    color = Color.Black,
                    fontSize = 10.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                // Item description table
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("ESPECIFICAÇÕES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("TOTAL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }

                    matchingOrders.forEach { item ->
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.pantyType} (Tam ${item.pantySize})",
                                    fontSize = 11.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = String.format(Locale("pt", "BR"), "R$ %,.2f", item.totalValue),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Text(
                                text = "=> Qtd: ${item.quantity} un x R$ ${item.pantyValue}",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }

                Text(
                    text = "--------------------------------------------------------",
                    color = Color.Black,
                    fontSize = 10.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VALOR TOTAL A PAGAR:", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(
                        text = String.format(Locale("pt", "BR"), "R$ %,.2f", totalToPay),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = Color.LightGray, modifier = Modifier.padding(top = 10.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    com.example.ui.utils.generateInvoicePdfAndShare(
                        context = context,
                        order = order,
                        matchingOrders = matchingOrders,
                        brandName = brandName
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Compartilhar Comanda", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fechar", color = Color.Black)
            }
        }
    )
}

enum class DeadlineStatus {
    COMPLETED,
    FUTURE,
    NEAR,       // 1 - 3 days
    TODAY,      // 0 days
    OVERDUE     // < 0 days
}

data class DeadlineInfo(
    val status: DeadlineStatus,
    val daysRemaining: Int,
    val label: String
)

fun getDeadlineInfo(orderTimestamp: Long, orderStatus: String): DeadlineInfo {
    if (orderStatus == "Concluído") {
        return DeadlineInfo(DeadlineStatus.COMPLETED, 0, "Concluído")
    }

    val now = System.currentTimeMillis()
    val todayCal = java.util.Calendar.getInstance()
    todayCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    todayCal.set(java.util.Calendar.MINUTE, 0)
    todayCal.set(java.util.Calendar.SECOND, 0)
    todayCal.set(java.util.Calendar.MILLISECOND, 0)
    val startOfToday = todayCal.timeInMillis

    val dueCal = java.util.Calendar.getInstance()
    dueCal.timeInMillis = orderTimestamp
    dueCal.set(java.util.Calendar.HOUR_OF_DAY, 0)
    dueCal.set(java.util.Calendar.MINUTE, 0)
    dueCal.set(java.util.Calendar.SECOND, 0)
    dueCal.set(java.util.Calendar.MILLISECOND, 0)
    val startOfDueDate = dueCal.timeInMillis

    val diffMillis = startOfDueDate - startOfToday
    val daysRemaining = (diffMillis / (1000 * 60 * 60 * 24)).toInt()

    return when {
        daysRemaining < 0 -> DeadlineInfo(DeadlineStatus.OVERDUE, daysRemaining, "Atrasada")
        daysRemaining == 0 -> DeadlineInfo(DeadlineStatus.TODAY, daysRemaining, "Vence Hoje")
        daysRemaining in 1..3 -> {
            val label = if (daysRemaining == 1) "Vence Amanhã" else "Vence em $daysRemaining dias"
            DeadlineInfo(DeadlineStatus.NEAR, daysRemaining, label)
        }
        else -> {
            val df = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
            DeadlineInfo(DeadlineStatus.FUTURE, daysRemaining, "Prazo: ${df.format(Date(orderTimestamp))}")
        }
    }
}

