package com.example.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.TransactionEntity
import com.example.data.OrderEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

fun generatePdfAndShare(
    context: Context,
    balance: Double,
    inflow: Double,
    outflow: Double,
    transactions: List<TransactionEntity>,
    orders: List<OrderEntity>,
    brandName: String
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()

        val primaryColor = Color.parseColor("#381A2C")
        val secondaryColor = Color.parseColor("#8E6E82")

        paint.color = primaryColor
        paint.textSize = 18f
        paint.isFakeBoldText = true
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText((brandName + " - RELATÓRIO OPERACIONAL").uppercase(Locale.getDefault()), 40f, 65f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("Demonstrativo Consolidado de Lucratividade, Produção e Custos", 40f, 85f, paint)

        val df = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        paint.color = secondaryColor
        canvas.drawText("Emitido em: " + df.format(Date()), 390f, 85f, paint)

        paint.strokeWidth = 1.5f
        paint.color = primaryColor
        canvas.drawLine(40f, 100f, 555f, 100f, paint)

        paint.color = primaryColor
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("Resumo Consolidado de Saúde Financeira", 40f, 130f, paint)

        paint.textSize = 11f
        paint.color = Color.BLACK
        paint.isFakeBoldText = false
        canvas.drawText(String.format("Faturamento Bruto (Receitas): R$ %,.2f", inflow), 50f, 160f, paint)
        canvas.drawText(String.format("Despesas Operacionais (Saídas): R$ %,.2f", outflow), 50f, 180f, paint)
        
        paint.color = primaryColor
        paint.isFakeBoldText = true
        paint.textSize = 11f
        canvas.drawText(String.format("Saldo em Caixa (Lucro Líquido): R$ %,.2f", balance), 50f, 210f, paint)

        paint.color = Color.DKGRAY
        paint.isFakeBoldText = false
        val totalPieces = orders.sumOf { it.quantity }
        val costPiece = if (totalPieces > 0) outflow / totalPieces else 0.0
        val margin = if (inflow > 0.0) (balance / inflow) * 100.0 else 0.0
        canvas.drawText(String.format("Margem Estimada de Rendimento: %,.1f%%", margin), 50f, 230f, paint)
        canvas.drawText(String.format("Volume Total Fabricado: %d peças", totalPieces), 50f, 250f, paint)
        canvas.drawText(String.format("Custo de Insumo Unitário Médio: R$ %,.2f", costPiece), 50f, 270f, paint)

        var currentY = 310f

        val ordersByWeek = orders.groupBy { it.week }
        val outflowsByWeek = transactions.filter { it.type == "OUTFLOW" }.groupBy { it.week }
        val allWeeks = (ordersByWeek.keys + outflowsByWeek.keys).distinct().sorted()

        allWeeks.forEach { week ->
            val wOrders = ordersByWeek[week] ?: emptyList()
            val wOutflows = outflowsByWeek[week] ?: emptyList()
            
            if (currentY > 730f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 60f
            }
            
            paint.color = primaryColor
            paint.textSize = 12f
            paint.isFakeBoldText = true
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("Detalhes: $week", 40f, currentY, paint)
            currentY += 15f
            
            if (wOrders.isNotEmpty()) {
                val boxHeight = 18f + (wOrders.size * 15f)
                if (currentY + boxHeight > 780f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 60f
                }
                
                paint.color = Color.parseColor("#E8F5E9")
                canvas.drawRect(40f, currentY, 555f, currentY + boxHeight, paint)
                
                paint.color = Color.parseColor("#2E7D32")
                paint.textSize = 10f
                paint.isFakeBoldText = true
                currentY += 12f
                canvas.drawText("Pedidos Realizados (Entradas) - Total: R$ ${String.format(Locale("pt", "BR"), "%,.2f", wOrders.sumOf { it.totalValue })}", 45f, currentY, paint)
                currentY += 15f
                
                paint.color = Color.BLACK
                paint.isFakeBoldText = false
                paint.textSize = 9f
                wOrders.forEach { o ->
                    canvas.drawText(o.clientName.take(20), 45f, currentY, paint)
                    canvas.drawText("${o.quantity} un - ${o.pantyType}".take(35), 180f, currentY, paint)
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(String.format("R$ %,.2f", o.totalValue), 545f, currentY, paint)
                    paint.textAlign = Paint.Align.LEFT
                    currentY += 15f
                }
                currentY += 5f
            }
            
            if (wOutflows.isNotEmpty()) {
                val boxHeight = 18f + (wOutflows.size * 15f)
                if (currentY + boxHeight > 780f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    currentY = 60f
                }
                
                paint.color = Color.parseColor("#FFEBEE")
                canvas.drawRect(40f, currentY, 555f, currentY + boxHeight, paint)
                
                paint.color = Color.parseColor("#C62828")
                paint.textSize = 10f
                paint.isFakeBoldText = true
                currentY += 12f
                canvas.drawText("Gastos (Saídas) - Total: R$ ${String.format(Locale("pt", "BR"), "%,.2f", wOutflows.sumOf { it.amount })}", 45f, currentY, paint)
                currentY += 15f
                
                paint.color = Color.BLACK
                paint.isFakeBoldText = false
                paint.textSize = 9f
                wOutflows.forEach { t ->
                    canvas.drawText(t.description.take(20), 45f, currentY, paint)
                    canvas.drawText(t.category.take(25), 180f, currentY, paint)
                    paint.textAlign = Paint.Align.RIGHT
                    canvas.drawText(String.format("R$ %,.2f", t.amount), 545f, currentY, paint)
                    paint.textAlign = Paint.Align.LEFT
                    currentY += 15f
                }
                currentY += 5f
            }
            
            currentY += 10f
        }

        pdfDocument.finishPage(page)

        val pdfDir = File(context.cacheDir, "pdf")
        if (!pdfDir.exists()) pdfDir.mkdirs()
        val file = File(pdfDir, "Relatorio_Producao.pdf")
        if (file.exists()) {
            file.delete()
        }
        val stream = FileOutputStream(file)
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        stream.close()

        Toast.makeText(context, "PDF pronto: " + file.name, Toast.LENGTH_SHORT).show()

        val authority = "${context.packageName}.fileprovider"
        val fileUri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Exportar Relatório Produção")
            putExtra(Intent.EXTRA_SUBJECT, "Relatório Geral - $brandName")
            putExtra(Intent.EXTRA_TEXT, "Segue anexo o Relatório Executivo de Produção - $brandName.")
            putExtra(Intent.EXTRA_STREAM, fileUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartilhar Relatório"))

    } catch (e: Exception) {
        android.util.Log.e("PdfGenerator", "Erro ao exportar PDF", e)
        Toast.makeText(context, "Erro ao exportar PDF: " + e.message, Toast.LENGTH_LONG).show()
    }
}

private fun drawInvoiceContent(
    canvas: android.graphics.Canvas,
    paint: Paint,
    order: OrderEntity,
    matchingOrders: List<OrderEntity>,
    brandName: String,
    scale: Float,
    imageWidth: Int
) {
    val darkPlumColor = Color.parseColor("#381A2C")
    val secondaryAccent = Color.parseColor("#8E6E82")
    val dividerColor = Color.parseColor("#D4C4CD")

    val itemCount = matchingOrders.size

    val headerFontSize = when {
        itemCount <= 3 -> 24f
        itemCount <= 6 -> 22f
        itemCount <= 11 -> 20f
        itemCount <= 16 -> 18f
        else -> 16f
    }
    val subHeaderFontSize = when {
        itemCount <= 3 -> 12f
        itemCount <= 6 -> 11f
        itemCount <= 11 -> 10f
        itemCount <= 16 -> 9.5f
        else -> 8.5f
    }
    val metadataFontSize = when {
        itemCount <= 3 -> 13f
        itemCount <= 6 -> 12f
        itemCount <= 11 -> 11f
        itemCount <= 16 -> 10f
        else -> 9f
    }
    val metadataLineHeight = when {
        itemCount <= 3 -> 24f
        itemCount <= 6 -> 20f
        itemCount <= 11 -> 18f
        else -> 15f
    }
    val tableTitleFontSize = when {
        itemCount <= 3 -> 14f
        itemCount <= 6 -> 12.5f
        itemCount <= 11 -> 11.5f
        itemCount <= 16 -> 10.5f
        else -> 9.5f
    }
    val itemTextSize = when {
        itemCount <= 3 -> 14f
        itemCount <= 6 -> 12f
        itemCount <= 11 -> 10f
        itemCount <= 16 -> 8.5f
        else -> 7.5f
    }
    val detailTextSize = when {
        itemCount <= 3 -> 12f
        itemCount <= 6 -> 10.5f
        itemCount <= 11 -> 9f
        itemCount <= 16 -> 7.5f
        else -> 6.5f
    }
    val itemStep1 = when {
        itemCount <= 3 -> 22f
        itemCount <= 6 -> 18f
        itemCount <= 11 -> 15f
        itemCount <= 16 -> 12f
        else -> 10f
    }
    val itemStep2 = when {
        itemCount <= 3 -> 36f
        itemCount <= 6 -> 30f
        itemCount <= 11 -> 25f
        itemCount <= 16 -> 18f
        else -> 14f
    }

    // Header
    paint.color = darkPlumColor
    paint.textSize = headerFontSize * scale
    paint.isFakeBoldText = true
    paint.textAlign = Paint.Align.CENTER
    canvas.drawText(brandName.uppercase(Locale.getDefault()), imageWidth / 2f, 70f * scale, paint)

    paint.textSize = subHeaderFontSize * scale
    paint.isFakeBoldText = true
    paint.color = secondaryAccent
    canvas.drawText("DOCUMENTO DE FECHAMENTO SEMANAL", imageWidth / 2f, (70f + headerFontSize * 1.1f) * scale, paint)

    val headerLineY = (70f + headerFontSize * 1.1f + 16f) * scale
    paint.strokeWidth = 1.5f * scale
    paint.color = darkPlumColor
    canvas.drawLine(40f * scale, headerLineY, 555f * scale, headerLineY, paint)

    // Metadata
    val clienteY = headerLineY + 25f * scale
    paint.textAlign = Paint.Align.LEFT
    paint.textSize = metadataFontSize * scale
    paint.isFakeBoldText = true
    paint.color = darkPlumColor
    canvas.drawText("CLIENTE:", 50f * scale, clienteY, paint)
    paint.isFakeBoldText = false
    paint.color = Color.BLACK
    canvas.drawText(order.clientName.uppercase(Locale.getDefault()), 120f * scale, clienteY, paint)

    val periodoY = clienteY + metadataLineHeight * scale
    paint.isFakeBoldText = true
    paint.color = darkPlumColor
    canvas.drawText("PERÍODO:", 50f * scale, periodoY, paint)
    paint.isFakeBoldText = false
    paint.color = Color.BLACK
    canvas.drawText(order.week, 120f * scale, periodoY, paint)

    val emissaoY = periodoY + metadataLineHeight * scale
    paint.isFakeBoldText = true
    paint.color = darkPlumColor
    canvas.drawText("EMISSÃO:", 50f * scale, emissaoY, paint)
    paint.isFakeBoldText = false
    paint.color = Color.BLACK
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    canvas.drawText(dateFormatter.format(Date(order.timestamp)), 120f * scale, emissaoY, paint)

    val divider2Y = emissaoY + 18f * scale
    paint.color = dividerColor
    paint.strokeWidth = 1f * scale
    canvas.drawLine(40f * scale, divider2Y, 555f * scale, divider2Y, paint)

    // Table heading
    val tableHeadY = emissaoY + 40f * scale
    paint.textSize = tableTitleFontSize * scale
    paint.isFakeBoldText = true
    paint.color = darkPlumColor
    canvas.drawText("ESPECIFICAÇÕES DOS PRODUTOS", 50f * scale, tableHeadY, paint)
    paint.textAlign = Paint.Align.RIGHT
    canvas.drawText("TOTAL", 545f * scale, tableHeadY, paint)

    val divider3Y = emissaoY + 48f * scale
    canvas.drawLine(40f * scale, divider3Y, 555f * scale, divider3Y, paint)

    // Items
    var currentY = emissaoY + 70f * scale
    matchingOrders.forEach { item ->
        paint.textSize = itemTextSize * scale
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = true
        paint.color = darkPlumColor
        canvas.drawText("${item.pantyType} (Tam ${item.pantySize})", 50f * scale, currentY, paint)

        paint.textAlign = Paint.Align.RIGHT
        val formattedTotal = String.format(Locale("pt", "BR"), "R$ %,.2f", item.totalValue)
        canvas.drawText(formattedTotal, 545f * scale, currentY, paint)

        currentY += itemStep1 * scale
        paint.textSize = detailTextSize * scale
        paint.textAlign = Paint.Align.LEFT
        paint.isFakeBoldText = false
        paint.color = Color.DKGRAY
        canvas.drawText("=> Quantidade: ${item.quantity} un  x  R$ ${String.format(Locale("pt", "BR"), "%,.2f", item.pantyValue)}", 60f * scale, currentY, paint)

        paint.color = Color.BLACK
        currentY += itemStep2 * scale
    }

    paint.color = dividerColor
    paint.strokeWidth = 1f * scale
    canvas.drawLine(40f * scale, currentY, 555f * scale, currentY, paint)
    currentY += 25f * scale

    // Grand total
    paint.textSize = (tableTitleFontSize + 1f) * scale
    paint.isFakeBoldText = true
    paint.textAlign = Paint.Align.LEFT
    paint.color = darkPlumColor
    canvas.drawText("VALOR TOTAL DO FECHAMENTO:", 50f * scale, currentY, paint)

    paint.textAlign = Paint.Align.RIGHT
    val grandTotalStr = String.format(Locale("pt", "BR"), "R$ %,.2f", matchingOrders.sumOf { it.totalValue })
    paint.color = Color.parseColor("#CC125C")
    canvas.drawText(grandTotalStr, 545f * scale, currentY, paint)
}

fun generateInvoicePdfAndShare(
    context: Context,
    order: OrderEntity,
    matchingOrders: List<OrderEntity>,
    brandName: String
) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        drawInvoiceContent(page.canvas, Paint(), order, matchingOrders, brandName, 1f, 595)

        pdfDocument.finishPage(page)

        val pdfDir = File(context.cacheDir, "pdf")
        if (!pdfDir.exists()) pdfDir.mkdirs()
        val comandaFile = File(pdfDir, "Comanda_${order.clientName.replace(" ", "_")}.pdf")
        if (comandaFile.exists()) comandaFile.delete()

        val stream = FileOutputStream(comandaFile)
        pdfDocument.writeTo(stream)
        pdfDocument.close()
        stream.close()

        val authority = "${context.packageName}.fileprovider"
        val comandaUri = FileProvider.getUriForFile(context, authority, comandaFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "Compartilhar Comanda de Entrega")
            putExtra(Intent.EXTRA_SUBJECT, "Comanda Semanal - $brandName")
            putExtra(Intent.EXTRA_TEXT, "Prezado cliente, segue fechamento e comanda da semana referente à sua produção de confecções da marca $brandName.")
            putExtra(Intent.EXTRA_STREAM, comandaUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Enviar Comanda Semanal"))

    } catch (e: Exception) {
        android.util.Log.e("PdfGenerator", "Erro ao gerar PDF da comanda", e)
        Toast.makeText(context, "Erro ao gerar PDF da comanda: " + e.message, Toast.LENGTH_LONG).show()
    }
}

fun generateInvoiceImageAndShare(
    context: Context,
    order: OrderEntity,
    matchingOrders: List<OrderEntity>,
    brandName: String
) {
    try {
        val imageWidth = 1080
        val scale = imageWidth / 595f
        val imageHeight = (842 * scale).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(imageWidth, imageHeight, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        drawInvoiceContent(canvas, Paint(), order, matchingOrders, brandName, scale, imageWidth)

        val imageDir = File(context.cacheDir, "images")
        if (!imageDir.exists()) imageDir.mkdirs()
        val imageFile = File(imageDir, "Comanda_${order.clientName.replace(" ", "_")}.jpg")
        if (imageFile.exists()) imageFile.delete()

        val stream = FileOutputStream(imageFile)
        bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 95, stream)
        stream.close()
        bitmap.recycle()

        val authority = "${context.packageName}.fileprovider"
        val imageUri = FileProvider.getUriForFile(context, authority, imageFile)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_TITLE, "Compartilhar Comanda de Entrega")
            putExtra(Intent.EXTRA_SUBJECT, "Comanda Semanal - $brandName")
            putExtra(Intent.EXTRA_TEXT, "Prezado cliente, segue fechamento e comanda da semana referente à sua produção de confecções da marca $brandName.")
            putExtra(Intent.EXTRA_STREAM, imageUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Enviar Comanda Semanal"))

    } catch (e: Exception) {
        android.util.Log.e("PdfGenerator", "Erro ao gerar imagem da comanda", e)
        Toast.makeText(context, "Erro ao gerar imagem da comanda: " + e.message, Toast.LENGTH_LONG).show()
    }
}
