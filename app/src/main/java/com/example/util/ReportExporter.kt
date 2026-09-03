package com.example.util

import android.content.Context
import android.content.Intent
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.model.MonthlyReportData
import java.util.Locale

object ReportExporter {

    fun shareReportAsText(context: Context, report: MonthlyReportData) {
        val sb = StringBuilder()
        sb.append("📊 تقرير إنجاز المهام الشهري: ${report.monthNameAr}\n")
        sb.append("━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("🎯 نسبة الإنجاز الكلية: ${String.format(Locale.US, "%.1f", report.overallCompletionRate)}%\n")
        sb.append("✅ المهام المنجزة: ${report.completedTasks} مهمة\n")
        sb.append("⏳ المهام المتبقية: ${report.uncompletedTasks} مهمة\n")
        sb.append("📋 إجمالي المهام: ${report.totalTasks} مهمة\n\n")

        report.bestWeek?.let {
            sb.append("🏆 أفضل أسبوع: ${it.labelAr} (${it.dateRangeAr}) بنسبة إنجاز ${String.format(Locale.US, "%.1f", it.completionRate)}%\n")
        }
        report.worstWeek?.let {
            sb.append("⚠️ أقل أسبوع: ${it.labelAr} (${it.dateRangeAr}) بنسبة إنجاز ${String.format(Locale.US, "%.1f", it.completionRate)}%\n")
        }

        if (report.mostPostponedTasks.isNotEmpty()) {
            sb.append("\n📌 أكثر المهام غير المنجزة تكراراً:\n")
            report.mostPostponedTasks.forEachIndexed { i, t ->
                sb.append("${i + 1}. ${t.title} [${t.category}] - تكررت ${t.uncompletedCount} مرات\n")
            }
        }

        if (report.categoryStats.isNotEmpty()) {
            sb.append("\n📁 الإنجاز حسب التصنيف:\n")
            report.categoryStats.forEach { c ->
                sb.append("• ${c.category}: ${c.completed}/${c.total} (${String.format(Locale.US, "%.0f", c.rate)}%)\n")
            }
        }

        sb.append("\n━━━━━━━━━━━━━━━━━━━━\n")
        sb.append("تم التصدير عبر تطبيق مهامي اليومية")

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, sb.toString())
            putExtra(Intent.EXTRA_SUBJECT, "تقرير إنجاز المهام الشهري - ${report.monthNameAr}")
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة التقرير الشهري")
        context.startActivity(shareIntent)
    }

    fun printOrSavePdf(context: Context, report: MonthlyReportData) {
        val htmlContent = generateHtmlReport(report)
        val webView = WebView(context)
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false

            override fun onPageFinished(view: WebView?, url: String?) {
                val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                val printAdapter = webView.createPrintDocumentAdapter("تقرير_المهام_${report.monthPrefix}")
                val jobName = "تقرير إنجاز ${report.monthNameAr}"
                val builder = PrintAttributes.Builder()
                builder.setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                builder.setColorMode(PrintAttributes.COLOR_MODE_COLOR)
                printManager?.print(jobName, printAdapter, builder.build())
            }
        }
        webView.loadDataWithBaseURL(null, htmlContent, "text/html; charset=UTF-8", "UTF-8", null)
    }

    private fun generateHtmlReport(report: MonthlyReportData): String {
        val rateStr = String.format(Locale.US, "%.1f", report.overallCompletionRate)
        val bestWeekText = report.bestWeek?.let {
            "${it.labelAr} (${it.dateRangeAr}) بنسبة <b>${String.format(Locale.US, "%.1f", it.completionRate)}%</b>"
        } ?: "لا توجد بيانات كافية"

        val worstWeekText = report.worstWeek?.let {
            "${it.labelAr} (${it.dateRangeAr}) بنسبة <b>${String.format(Locale.US, "%.1f", it.completionRate)}%</b>"
        } ?: "لا توجد بيانات كافية"

        val postponedRows = report.mostPostponedTasks.joinToString("") {
            "<tr><td>${it.title}</td><td>${it.category}</td><td style='color:#ef4444;font-weight:bold;'>${it.uncompletedCount} مرات</td></tr>"
        }

        val categoryRows = report.categoryStats.joinToString("") {
            "<tr><td>${it.category}</td><td>${it.total}</td><td>${it.completed}</td><td><b>${String.format(Locale.US, "%.0f", it.rate)}%</b></td></tr>"
        }

        return """
        <!DOCTYPE html>
        <html dir="rtl" lang="ar">
        <head>
            <meta charset="UTF-8">
            <title>تقرير المهام الشهري - ${report.monthNameAr}</title>
            <style>
                body { font-family: sans-serif; margin: 30px; color: #0f172a; direction: rtl; }
                h1 { color: #0d9488; margin-bottom: 4px; }
                .subtitle { color: #64748b; font-size: 14px; margin-bottom: 24px; }
                .cards-row { display: flex; gap: 15px; margin-bottom: 25px; }
                .card { flex: 1; padding: 18px; border-radius: 12px; background: #f8fafc; border: 1px solid #e2e8f0; text-align: center; }
                .card-val { font-size: 28px; font-weight: bold; margin-top: 6px; }
                .rate { color: #0d9488; }
                .done { color: #10b981; }
                .pending { color: #ef4444; }
                .section-title { font-size: 18px; font-weight: bold; margin-top: 25px; margin-bottom: 12px; color: #1e293b; border-bottom: 2px solid #0d9488; padding-bottom: 6px; }
                .info-box { background: #f1f5f9; padding: 14px 18px; border-radius: 8px; margin-bottom: 15px; }
                table { width: 100%; border-collapse: collapse; margin-top: 8px; }
                th, td { border: 1px solid #cbd5e1; padding: 10px 14px; text-align: right; }
                th { background-color: #f8fafc; color: #334155; }
                .footer { margin-top: 40px; font-size: 12px; color: #94a3b8; text-align: center; border-top: 1px solid #e2e8f0; padding-top: 15px; }
            </style>
        </head>
        <body>
            <h1>📊 تقرير إنجاز المهام الشهري</h1>
            <div class="subtitle">تقرير مفصل لشهر: ${report.monthNameAr}</div>

            <div class="cards-row">
                <div class="card">
                    <div>نسبة الإنجاز الكلية</div>
                    <div class="card-val rate">$rateStr%</div>
                </div>
                <div class="card">
                    <div>المهام المنجزة</div>
                    <div class="card-val done">${report.completedTasks}</div>
                </div>
                <div class="card">
                    <div>المهام المتبقية</div>
                    <div class="card-val pending">${report.uncompletedTasks}</div>
                </div>
                <div class="card">
                    <div>إجمالي المهام</div>
                    <div class="card-val">${report.totalTasks}</div>
                </div>
            </div>

            <div class="section-title">📅 تقييم الأسابيع</div>
            <div class="info-box">
                <p>🏆 <b>أفضل أسبوع:</b> $bestWeekText</p>
                <p>⚠️ <b>أقل أسبوع:</b> $worstWeekText</p>
            </div>

            ${if (report.mostPostponedTasks.isNotEmpty()) """
            <div class="section-title">📌 أكثر المهام غير المنجزة تكراراً</div>
            <table>
                <thead>
                    <tr><th>المهمة</th><th>التصنيف</th><th>عدد مرات عدم الإنجاز</th></tr>
                </thead>
                <tbody>
                    $postponedRows
                </tbody>
            </table>
            """ else ""}

            ${if (report.categoryStats.isNotEmpty()) """
            <div class="section-title">📁 تفصيل الإنجاز حسب التصنيف</div>
            <table>
                <thead>
                    <tr><th>التصنيف</th><th>إجمالي المهام</th><th>المنجزة</th><th>النسبة</th></tr>
                </thead>
                <tbody>
                    $categoryRows
                </tbody>
            </table>
            """ else ""}

            <div class="footer">
                تم استخراج هذا التقرير تلقائياً بواسطة تطبيق "مهامي اليومية" • تاريخ الاستخراج: ${DateUtils.getTodayDateString()}
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
