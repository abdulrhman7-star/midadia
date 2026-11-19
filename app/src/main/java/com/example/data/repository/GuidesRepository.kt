package com.example.data.repository

import com.example.data.model.GuideArticle
import com.example.data.model.GuideSection

object GuidesRepository {
    val GUIDE_ARTICLES: List<GuideArticle> = listOf(
        GuideArticle(
            slug = "ai-markdown-to-word-guide",
            categoryId = "formatting",
            categoryAr = "إنتاجية المستندات",
            categoryEn = "Document Workflows",
            publishedAt = "2026-10-18",
            readingMinutes = 6,
            titleAr = "الدليل الشامل لتحويل نصوص ChatGPT وClaude وGemini إلى مستندات Word احترافية",
            titleEn = "Complete Guide to Converting ChatGPT, Claude & Gemini Markdown into Word Documents",
            excerptAr = "تعرف على الأسباب التقنية لتشوه التنسيق عند النسخ المباشر من نماذج الذكاء الاصطناعي، وكيفية تصدير تقارير متكاملة مع العناوين والجداول والأكواد بضغطة زر.",
            excerptEn = "Learn why direct copy-pasting from AI assistants breaks formatting in Microsoft Word, and how to export clean reports with headings, tables, and code blocks.",
            authorAr = "فريق التحرير التقني — مداد",
            authorEn = "midadd Editorial Team",
            sections = listOf(
                GuideSection(
                    id = "why-copy-breaks",
                    headingAr = "1. لماذا يتشوه التنسيق عند النسخ المباشر من نصوص الذكاء الاصطناعي؟",
                    headingEn = "1. Why Direct Copy-Paste Breaks Document Layout",
                    paragraphsAr = listOf(
                        "تعتمد معظم نماذج الذكاء الاصطناعي الحديثة مثل ChatGPT وClaude وGemini على صيغة Markdown لتنظيم المخرجات؛ وهي لغة توصيف نصية تستخدم علامات مثل (#) للعناوين و(**) للخط العريض و(|) لبناء الجداول. وعندما يقوم المستخدم بتحديد النص من المتصفح ولصقه مباشرة في Microsoft Word، ينسخ المتصفح أنماط HTML الخاصة بواجهة الدردشة نفسها.",
                        "الأهم من ذلك هو فقدان البنية الدلالية للمستند (Document Hierarchy)؛ فبدلاً من إنشاء أنماط عناوين حقيقية (Heading 1, Heading 2)، تتحول العناوين إلى نصوص عادية مكبرة يدوياً وتفقد الجداول حدودها الواضحة."
                    ),
                    paragraphsEn = listOf(
                        "Modern AI assistants output responses structured in Markdown—using # for headings, ** for bold emphasis, and pipe syntax for tables. Copying directly from a browser chat window carries over web-specific CSS classes and dark backgrounds into Word.",
                        "Even worse, semantic hierarchy is often lost: headings become manually enlarged paragraphs rather than native Heading 1/2 styles, breaking automatic tables of contents and print pagination."
                    )
                ),
                GuideSection(
                    id = "clean-workflow",
                    headingAr = "2. الخطوات العملية لتجهيز مستند Word متكامل من مخرجات الذكاء الاصطناعي",
                    headingEn = "2. Practical Workflow for Clean AI-to-DOCX Exports",
                    paragraphsAr = listOf(
                        "للحصول على ملف (.docx) جاهز للطباعة والتعديل دون الحاجة لإعادة التنسيق يدوياً، يُنصح بنسخ النص بصيغة Markdown الخام باستخدام زر النسخ المخصص، ثم تمريره عبر محرك مداد التحويلي."
                    ),
                    paragraphsEn = listOf(
                        "Instead of highlighting rendered chat bubbles, always use the dedicated Copy button at the bottom of the AI message to capture raw Markdown, then pass it through midadd's OOXML structure builder."
                    ),
                    bulletsAr = listOf(
                        "استخدم زر النسخ الخام للمحادثة بدلاً من التحديد اليدوي للحفاظ على علامات الجداول والمعادلات.",
                        "تأكد من مطابقة مستويات العناوين (#، ##، ###) لتظهر تلقائياً في جزء التنقل داخل Microsoft Word.",
                        "حدد اتجاه المستند الأساسي (RTL للعربية أو LTR للإنجليزية) قبل التصدير لضبط محاذاة الترقيم والقوائم.",
                        "راجع الهوامش وحجم الورق من شريط التخطيط لضمان تطابق المعاينة مع الطباعة."
                    ),
                    bulletsEn = listOf(
                        "Copy raw Markdown via the assistant copy icon to preserve table delimiters and math blocks.",
                        "Use consistent heading levels (#, ##, ###) so Word builds the Navigation Pane automatically.",
                        "Set the base direction (RTL for Arabic, LTR for English) before export to align lists and numbering.",
                        "Configure A4 margins and orientation from the Layout Ribbon before exporting as DOCX or PDF."
                    )
                ),
                GuideSection(
                    id = "code-example",
                    headingAr = "3. مثال عملي على بنية Markdown القياسية الصالحة للتصدير",
                    headingEn = "3. Example of Clean Export-Ready Markdown Structure",
                    paragraphsAr = listOf(
                        "يوضح المثال التالي كيف يمكنك تنظيم التقرير باستخدام Markdown قياسي ليتم تحويله إلى مستند Word يحتوي على عنوان رئيسي، قائمة، وجدول بيانات منسق:"
                    ),
                    paragraphsEn = listOf(
                        "The snippet below demonstrates how clean Markdown converts directly into structured Word headings, lists, and bordered tables:"
                    ),
                    codeExample = "# التقرير الفصلي للأداء التقني\n\n## ملخص النتائج الرئيسية\n- تحسن سرعة الاستجابة بنسبة **34%** خلال الربع الثالث.\n- تقليص أخطاء المعالجة الطرفية إلى أقل من `0.2%`.\n\n| المؤشر | الربع الثاني | الربع الثالث |\n| :--- | :--- | :--- |\n| زمن المعالجة | 420ms | 275ms |\n| رضا المستخدمين | 91% | 97% |"
                )
            )
        ),
        GuideArticle(
            slug = "latex-omml-arabic-research-guide",
            categoryId = "math",
            categoryAr = "البحث العلمي والمعادلات",
            categoryEn = "Academic & Math",
            publishedAt = "2026-10-25",
            readingMinutes = 7,
            titleAr = "تحويل معادلات LaTeX إلى معادلات Word الأصلية (OMML) في الأبحاث العربية",
            titleEn = "Converting LaTeX Equations into Native Editable Word Math (OMML)",
            excerptAr = "لماذا تفشل الطريقة التقليدية بلصق المعادلات كصور؟ وكيف يحول محرك OMML رموز التكامل والكسور والمصفوفات إلى معادلات قابلة للتعديل داخل Word.",
            excerptEn = "Why pasting equations as screenshots hurts academic papers, and how LaTeX-to-OMML conversion creates crisp, editable native Word math elements.",
            authorAr = "د. سارة العتيبي — مستشارة النشر الأكاديمي",
            authorEn = "Dr. Sara Al-Otaibi — Academic Publishing Advisor",
            sections = listOf(
                GuideSection(
                    id = "what-is-omml",
                    headingAr = "1. الفرق بين الصور الثابتة ومعادلات Word الأصلية (OMML)",
                    headingEn = "1. Static Screenshots vs. Native Office Math (OMML)",
                    paragraphsAr = listOf(
                        "عندما يطلب الباحثون من نماذج الذكاء الاصطناعي كتابة اشتقاقات رياضية، يتم توليد المعادلات بصيغة LaTeX محاطة بعلامات الدولار ($ أو $$). المحولات البدائية تحولها إلى صور نقطية تتشوش عند التكبير ولا يمكن تعديلها.",
                        "في المقابل، يعتمد معيار Microsoft Word (.docx) على لغة توصيف رياضية قياسية تُسمى Office Math Markup Language (OMML)، مما يجعل المعادلة كائناً رياضياً قابلاً للتعديل بالكامل داخل Word."
                    ),
                    paragraphsEn = listOf(
                        "AI assistants emit mathematics using LaTeX syntax wrapped in $ or $$ delimiters. Basic converters rasterize formulas into low-DPI images that pixelate in print and cannot be edited by reviewers.",
                        "Microsoft Word natively uses Office Math Markup Language (OMML). Converting LaTeX directly into OMML nodes turns every fraction, integral, and matrix into a first-class Word equation object."
                    )
                ),
                GuideSection(
                    id = "inline-vs-block",
                    headingAr = "2. قواعد كتابة المعادلات السطرية والمعادلات المستقلة",
                    headingEn = "2. Best Practices for Inline vs. Display Equations",
                    paragraphsAr = listOf(
                        "لضمان أقصى دقة في التحويل، يجب التمييز بين الصيغ السطرية المدمجة وسط الفقرة والمعادلات المستقلة المتمركزة."
                    ),
                    paragraphsEn = listOf(
                        "To ensure clean rendering, distinguish between inline notation embedded within prose and standalone display blocks centered on their own line."
                    ),
                    bulletsAr = listOf(
                        "استخدم علامة دولار واحدة (\$E = mc^2\$) للمتغيرات والرموز المدمجة وسط الفقرة.",
                        "استخدم علامتي دولار ($$ ... $$) في سطر مستقل للمعادلات الطويلة والكسور.",
                        "تجنب وضع نصوص عربية طويلة داخل أقواس \\text{} دون الحاجة.",
                        "احرص على توازن الأقواس المعقوفة {} في الكسور \\frac{numerator}{denominator} والجذور \\sqrt{x}."
                    ),
                    bulletsEn = listOf(
                        "Use single dollar signs (\$E = mc^2\$) for inline variables inside paragraphs.",
                        "Use double dollar blocks ($$ ... $$) on separate lines for complex fractions and integrals.",
                        "Keep prose explanations outside math delimiters so bidirectional text wraps naturally.",
                        "Verify matching curly braces in \\frac{a}{b} and \\sqrt{x} expressions."
                    ),
                    codeExample = "$$\n\\int_{0}^{\\infty} e^{-x^2} \\, dx = \\frac{\\sqrt{\\pi}}{2}\n$$"
                ),
                GuideSection(
                    id = "arabic-papers-tips",
                    headingAr = "3. نصائح تنسيق الأبحاث العلمية والرسائل الجامعية باللغة العربية",
                    headingEn = "3. Formatting Arabic Theses and Technical Papers",
                    paragraphsAr = listOf(
                        "في الأبحاث المكتوبة بالعربية، تبقى الرموز الرياضية القياسية بحروفها اللاتينية ومن اليسار إلى اليمين حتى داخل فقرة عربية RTL، لذا يعزل محرك مداد المعادلة في اتجاه LTR لحماية ترتيب البسط والمقام وحدود التكامل."
                    ),
                    paragraphsEn = listOf(
                        "Even in Arabic RTL manuscripts, international mathematical expressions read left-to-right. Isolating OMML blocks in LTR direction prevents subscripts and fraction layouts from flipping."
                    )
                )
            )
        ),
        GuideArticle(
            slug = "fixing-arabic-rtl-tables-word-guide",
            categoryId = "arabic-rtl",
            categoryAr = "دعم العربية والـ RTL",
            categoryEn = "Arabic & RTL Typography",
            publishedAt = "2026-11-02",
            readingMinutes = 5,
            titleAr = "حل مشاكل اتجاه النص العربي (RTL) والجداول المنسوخة في Word وGoogle Docs",
            titleEn = "Fixing Arabic RTL Alignment and Mixed-Direction Tables in Word & Google Docs",
            excerptAr = "معالجة جذرية لمشاكل انعكاس الترقيم، تداخل النصوص العربية والإنجليزية (Bidi)، وتنسيق خلايا الجداول في التقارير الإدارية والضريبية.",
            excerptEn = "Solve flipped punctuation, bidirectional Arabic/English text overlap, and broken table columns in business reports and official documents.",
            authorAr = "م. طارق المنصور — مهندس تنسيق النصوص ثنائية الاتجاه",
            authorEn = "Tariq Al-Mansour — Bidirectional Typography Engineer",
            sections = listOf(
                GuideSection(
                    id = "bidi-challenges",
                    headingAr = "1. لماذا تنعكس علامات الترقيم والأقواس في النصوص ثنائية الاتجاه؟",
                    headingEn = "1. Why Punctuation and Parentheses Flip in Mixed Arabic/English Text",
                    paragraphsAr = listOf(
                        "عند ورود مصطلحات إنجليزية أو أرقام وسط الجملة العربية مع ضبط الفقرة على LTR رغم المحاذاة يميناً، تقفز علامات الترقيم وتنعكس الأقواس. الحل هو تطبيق خاصية w:bidi المنطقية في ملف Word نفسه."
                    ),
                    paragraphsEn = listOf(
                        "Modern Arabic reports frequently mix Latin acronyms and numbers inside Arabic sentences. Applying right alignment alone on an LTR paragraph causes periods and parentheses to jump. The true fix sets logical w:bidi direction in OOXML."
                    )
                ),
                GuideSection(
                    id = "tables-in-arabic",
                    headingAr = "2. ضبط اتجاه الجداول وترتيب الأعمدة في التقارير العربية",
                    headingEn = "2. Configuring RTL Table Column Flow and Cell Padding",
                    paragraphsAr = listOf(
                        "تتيح خصائص الجداول الثنائية (bidiVisual) قلب ترتيب الأعمدة تلقائياً لتبدأ من اليمين إلى اليسار مع الحفاظ على محاذاة الأرقام المالية بوضوح."
                    ),
                    paragraphsEn = listOf(
                        "In Arabic financial or comparative tables, the primary label column should anchor on the right edge and flow leftward. Enabling bidiVisual table semantics in Word ensures natural column ordering."
                    ),
                    bulletsAr = listOf(
                        "اختر خطوطاً عربية واضحة ومصممة للطباعة مثل Cairo أو Amiri.",
                        "استخدم تباعد أسطر مريح لتفادي تداخل التشكيل والحروف السفلية.",
                        "فعّل الحدود الكاملة والهوامش الداخلية لمنع التصاق النص بحدود الخلية.",
                        "استخدم زر نسخ لـ Google Docs للحفاظ على الجداول عند المتابعة السحابية."
                    ),
                    bulletsEn = listOf(
                        "Pair Arabic headings with legible fonts such as Cairo or Amiri.",
                        "Use comfortable line height to give Arabic diacritics clean breathing room.",
                        "Add consistent cell padding and hairline table borders for readability.",
                        "Use Copy for Google Docs to preserve table borders in cloud documents."
                    )
                )
            )
        )
    )
}
