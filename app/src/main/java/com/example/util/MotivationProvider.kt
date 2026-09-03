package com.example.util

import java.util.Calendar

object MotivationProvider {

    private val MOTIVATIONAL_QUOTES = listOf(
        "خطوة صغيرة ومستمرة كل يوم تصنع فارقاً عظيماً على مدار العام.",
        "سر النجاح يكمن في البدء، فابدأ بأهم مهمة لديك الآن دون تردد.",
        "الإنتاجية ليست قضاء ساعات طويلة، بل إتقان الأولويات بتركيز وذكاء.",
        "كل دقيقة تقضيها في تنظيم يومك توفر عليك ساعات من التشتت والارتباك.",
        "العزيمة والإصرار هما الوقود الحقيقي لتحويل الأهداف إلى واقع ملموس.",
        "لا تؤجل عمل اليوم إلى الغد، فالغد لديه أهدافه وفرصه الخاصة.",
        "الإنجاز اليومي يولد السعادة الحقيقية ويزيدك ثقة وقدرة على المتابعة.",
        "ركز على التقدم المستمر يوماً بعد يوم، فالاستمرارية هي سر التميز.",
        "يوم جديد يعني فرصة متجددة لكتابة صفحة نجاح ناصعة في حياتك.",
        "كل مهمة تنجزها الآن تقربك خطوة حقيقية نحو طموحاتك الكبرى.",
        "لا تنتظر الوقت المثالي لتكون منتجاً، بل اجعل الوقت الحالي مثالياً بالعمل.",
        "الصبر والانضباط هما الجسر الذي يربط بين نيتك وإنجازك الفعلي."
    )

    fun getRandomQuote(currentIndex: Int = -1): Pair<Int, String> {
        var nextIndex = (0 until MOTIVATIONAL_QUOTES.size).random()
        if (MOTIVATIONAL_QUOTES.size > 1 && nextIndex == currentIndex) {
            nextIndex = (nextIndex + 1) % MOTIVATIONAL_QUOTES.size
        }
        return Pair(nextIndex, MOTIVATIONAL_QUOTES[nextIndex])
    }

    fun getTimeBasedGreeting(): Pair<String, String> {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> Pair("صباح النشاط والبدايات الموفقة ☀️", "ابدأ يومك بتحديد الأولويات لإنجاز منظم")
            in 12..16 -> Pair("طاب يومك بكل همة وتركيز 🌤️", "واصل نشاطك وتذكر أخذ فترات استراحة قصيرة")
            in 17..21 -> Pair("مساء الإنجاز والهمة العالية 🌇", "راجع ما أنجزته واستكمل بقية مهامك اليوم")
            else -> Pair("ليلة هادئة ومراجعة مباركة 🌙", "قيّم إنجازاتك اليومية واستعد بنشاط ليوم غد")
        }
    }
}
