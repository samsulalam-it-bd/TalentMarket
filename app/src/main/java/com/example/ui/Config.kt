package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.Color

data class JobCategory(
    val id: Int,
    val nameEn: String,
    val nameAr: String,
    val sector: String,
    val icon: ImageVector,
    val colorHex: String
)

object ConfigData {
    val gulfCountriesWithCities = mapOf(
        "Saudi Arabia" to listOf("Riyadh", "Jeddah", "Mecca", "Medina", "Dammam", "Khobar", "Abha", "Tabuk"),
        "United Arab Emirates" to listOf("Dubai", "Abu Dhabi", "Sharjah", "Ajman", "Ras Al Khaimah", "Fujairah"),
        "Qatar" to listOf("Doha", "Al Rayyan", "Al Wakrah", "Al Khor", "Umm Salal"),
        "Kuwait" to listOf("Kuwait City", "Al Ahmadi", "Hawalli", "Farwaniya", "Jahra"),
        "Oman" to listOf("Muscat", "Salalah", "Sohar", "Nizwa", "Sur"),
        "Bahrain" to listOf("Manama", "Muharraq", "Riffa", "Hamad Town", "Isa Town")
    )

    val translationEnToAr = mapOf(
        "Saudi Arabia" to "المملكة العربية السعودية",
        "United Arab Emirates" to "الإمارات العربية المتحدة",
        "Qatar" to "قطر",
        "Kuwait" to "الكويت",
        "Oman" to "عمان",
        "Bahrain" to "البحرين",
        "Riyadh" to "الرياض",
        "Jeddah" to "جدة",
        "Mecca" to "مكة المكرمة",
        "Medina" to "المدينة المنورة",
        "Dammam" to "الدمام",
        "Khobar" to "الخبر",
        "Abha" to "أبها",
        "Tabuk" to "تبوك",
        "Dubai" to "دبي",
        "Abu Dhabi" to "أبو ظبي",
        "Sharjah" to "الشرالقة",
        "Ajman" to "عجمان",
        "Ras Al Khaimah" to "رأس الخيمة",
        "Fujairah" to "الفجيرة",
        "Doha" to "الدوحة",
        "Al Rayyan" to "الريان",
        "Al Wakrah" to "الوكرة",
        "Al Khor" to "الخور",
        "Umm Salal" to "أم صلال",
        "Kuwait City" to "مدينة الكويت",
        "Al Ahmadi" to "الأحمدي",
        "Hawalli" to "حولي",
        "Farwaniya" to "الفروانية",
        "Jahra" to "الجهراء",
        "Muscat" to "مسقط",
        "Salalah" to "صلالة",
        "Sohar" to "صحار",
        "Nizwa" to "نزوى",
        "Sur" to "صور",
        "Manama" to "المنامة",
        "Muharraq" to "المحرق",
        "Riffa" to "الرفاع",
        "Hamad Town" to "مدينة حمد",
        "Isa Town" to "مدينة عيسى"
    )

    fun translate(text: String, lang: String): String {
        if (lang == "ar") {
            return translationEnToAr[text] ?: text
        }
        return text
    }

    val saudiCategories = listOf(
        // Construction
        JobCategory(1, "Construction", "البناء والإنشاء", "Construction", Icons.Default.Construction, "#E6F1FB"),
        JobCategory(2, "Plumbing", "السباكة", "Construction", Icons.Default.Plumbing, "#E6F1FB"),
        JobCategory(3, "Electrical Work", "الكهرباء", "Construction", Icons.Default.ElectricalServices, "#E6F1FB"),
        JobCategory(4, "Carpentry", "النجارة", "Construction", Icons.Default.Handyman, "#E6F1FB"),
        JobCategory(5, "Painting", "الدهان", "Construction", Icons.Default.FormatPaint, "#E6F1FB"),
        JobCategory(6, "Welding", "اللحام", "Construction", Icons.Default.LocalFireDepartment, "#E6F1FB"),
        JobCategory(7, "AC & Cooling", "التكييف والتبريد", "Construction", Icons.Default.AcUnit, "#E6F1FB"),
        JobCategory(8, "Tiling & Flooring", "التبليط والأرضيات", "Construction", Icons.Default.GridView, "#E6F1FB"),

        // IT & Tech
        JobCategory(9, "Software Engineer", "مهندس برمجيات", "IT & Tech", Icons.Default.Code, "#EAF3DE"),
        JobCategory(10, "Web Developer", "مطور مواقع", "IT & Tech", Icons.Default.Language, "#EAF3DE"),
        JobCategory(11, "Mobile Developer", "مطور تطبيقات", "IT & Tech", Icons.Default.PhoneAndroid, "#EAF3DE"),
        JobCategory(12, "UI/UX Designer", "مصمم واجهات", "IT & Tech", Icons.Default.DesignServices, "#EAF3DE"),
        JobCategory(13, "Data Analyst", "محلل بيانات", "IT & Tech", Icons.Default.BarChart, "#EAF3DE"),
        JobCategory(14, "Cybersecurity", "الأمن السيبراني", "IT & Tech", Icons.Default.Security, "#EAF3DE"),
        JobCategory(15, "Network Engineer", "مهندس شبكات", "IT & Tech", Icons.Default.Router, "#EAF3DE"),
        JobCategory(16, "IT Support", "دعم تقني", "IT & Tech", Icons.Default.SupportAgent, "#EAF3DE"),

        // Healthcare
        JobCategory(17, "Doctor", "طبيب", "Healthcare", Icons.Default.LocalHospital, "#FAECE7"),
        JobCategory(18, "Nurse", "ممرض / ممرضة", "Healthcare", Icons.Default.Healing, "#FAECE7"),
        JobCategory(19, "Pharmacist", "صيدلاني", "Healthcare", Icons.Default.Medication, "#FAECE7"),
        JobCategory(20, "Lab Technician", "تقني مختبر", "Healthcare", Icons.Default.Science, "#FAECE7"),
        JobCategory(21, "Dentist", "طبيب أسنان", "Healthcare", Icons.Default.MedicalServices, "#FAECE7"),
        JobCategory(22, "Physiotherapist", "معالج فيزيائي", "Healthcare", Icons.Default.FitnessCenter, "#FAECE7"),
        JobCategory(23, "Radiologist", "أخصائي أشعة", "Healthcare", Icons.Default.MonitorHeart, "#FAECE7"),
        JobCategory(24, "Caregiver", "مقدم رعاية", "Healthcare", Icons.Default.VolunteerActivism, "#FAECE7"),

        // Transport
        JobCategory(25, "Car Driver", "سائق سيارة", "Transport", Icons.Default.DirectionsCar, "#FAEEDA"),
        JobCategory(26, "Truck Driver", "سائق شاحنة", "Transport", Icons.Default.LocalShipping, "#FAEEDA"),
        JobCategory(27, "Delivery Rider", "موصل طلبات", "Transport", Icons.Default.DeliveryDining, "#FAEEDA"),
        JobCategory(28, "Bus Driver", "سائق حافلة", "Transport", Icons.Default.DirectionsBus, "#FAEEDA"),
        JobCategory(29, "Forklift Operator", "مشغل رافعة شوكية", "Transport", Icons.Default.Settings, "#FAEEDA"),
        JobCategory(30, "Logistics", "اللوجستيات", "Transport", Icons.Default.Inventory, "#FAEEDA"),

        // Finance
        JobCategory(31, "Accountant", "محاسب", "Finance", Icons.Default.Calculate, "#EEEDFE"),
        JobCategory(32, "Financial Analyst", "محلل مالي", "Finance", Icons.Default.TrendingUp, "#EEEDFE"),
        JobCategory(33, "Auditor", "مراجع حسابات", "Finance", Icons.Default.FactCheck, "#EEEDFE"),
        JobCategory(34, "Bank Teller", "أمين الصندوق", "Finance", Icons.Default.AccountBalance, "#EEEDFE"),
        JobCategory(35, "Insurance Agent", "وكيل تأمين", "Finance", Icons.Default.VerifiedUser, "#EEEDFE"),
        JobCategory(36, "Tax Consultant", "مستشار ضرائب", "Finance", Icons.Default.ReceiptLong, "#EEEDFE"),

        // Education
        JobCategory(37, "Teacher", "معلم", "Education", Icons.Default.School, "#E1F5EE"),
        JobCategory(38, "Tutor", "مدرس خصوصي", "Education", Icons.Default.MenuBook, "#E1F5EE"),
        JobCategory(39, "University Lecturer", "أستاذ جامعي", "Education", Icons.Default.AccountBalance, "#E1F5EE"),
        JobCategory(40, "Trainer", "مدرب", "Education", Icons.Default.WorkspacePremium, "#E1F5EE"),
        JobCategory(41, "Special Education", "تربية خاصة", "Education", Icons.Default.AccessibilityNew, "#E1F5EE"),

        // Food & Hospitality
        JobCategory(42, "Chef / Cook", "طاهي", "Food & Hospitality", Icons.Default.Restaurant, "#FBEAF0"),
        JobCategory(43, "Waiter", "نادل", "Food & Hospitality", Icons.Default.RoomService, "#FBEAF0"),
        JobCategory(44, "Barista", "باريستا", "Food & Hospitality", Icons.Default.Coffee, "#FBEAF0"),
        JobCategory(45, "Hotel Receptionist", "موظف استقبال فندقي", "Food & Hospitality", Icons.Default.Hotel, "#FBEAF0"),
        JobCategory(46, "Housekeeping", "خدمة الغرف", "Food & Hospitality", Icons.Default.CleaningServices, "#FBEAF0"),
        JobCategory(47, "Event Manager", "مدير فعاليات", "Food & Hospitality", Icons.Default.Event, "#FBEAF0"),
        JobCategory(48, "Catering", "خدمة تقديم الطعام", "Food & Hospitality", Icons.Default.SetMeal, "#FBEAF0"),

        // Sales & Marketing
        JobCategory(49, "Sales Executive", "مسؤول مبيعات", "Sales & Marketing", Icons.Default.PointOfSale, "#F1EFE8"),
        JobCategory(50, "Marketing Manager", "مدير تسويق", "Sales & Marketing", Icons.Default.Campaign, "#F1EFE8"),
        JobCategory(51, "Social Media", "مدير وسائل التواصل", "Sales & Marketing", Icons.Default.Share, "#F1EFE8"),
        JobCategory(52, "Content Creator", "صانع محتوى", "Sales & Marketing", Icons.Default.VideoCameraBack, "#F1EFE8"),
        JobCategory(53, "Graphic Designer", "مصمم جرافيك", "Sales & Marketing", Icons.Default.Brush, "#F1EFE8"),
        JobCategory(54, "Photographer", "مصور فوتوغرافي", "Sales & Marketing", Icons.Default.CameraAlt, "#F1EFE8"),
        JobCategory(55, "Brand Manager", "مدير العلامة التجارية", "Sales & Marketing", Icons.Default.Label, "#F1EFE8"),

        // Oil & Energy
        JobCategory(56, "Oil & Gas Engineer", "مهندس نفط وغاز", "Oil & Energy", Icons.Default.OilBarrel, "#FAEEDA"),
        JobCategory(57, "Petroleum Technician", "تقني بترول", "Oil & Energy", Icons.Default.Settings, "#FAEEDA"),
        JobCategory(58, "Solar Energy", "الطاقة الشمسية", "Oil & Energy", Icons.Default.WbSunny, "#FAEEDA"),
        JobCategory(59, "Drilling Operator", "مشغل حفر", "Oil & Energy", Icons.Default.Hardware, "#FAEEDA"),
        JobCategory(60, "Pipeline Inspector", "مفتش أنابيب", "Oil & Energy", Icons.Default.LinearScale, "#FAEEDA"),

        // Security
        JobCategory(61, "Security Guard", "حارس أمن", "Security", Icons.Default.Security, "#FCEBEB"),
        JobCategory(62, "CCTV Operator", "مشغل كاميرات مراقبة", "Security", Icons.Default.Videocam, "#FCEBEB"),
        JobCategory(63, "Firefighter", "رجل إطفاء", "Security", Icons.Default.LocalFireDepartment, "#FCEBEB"),
        JobCategory(64, "Civil Defense", "الدفاع المدني", "Security", Icons.Default.Emergency, "#FCEBEB"),

        // Retail
        JobCategory(65, "Retail Salesman", "بائع تجزئة", "Retail", Icons.Default.ShoppingBag, "#E1F5EE"),
        JobCategory(66, "Cashier", "أمين صندوق", "Retail", Icons.Default.PointOfSale, "#E1F5EE"),
        JobCategory(67, "Store Manager", "مدير متجر", "Retail", Icons.Default.Store, "#E1F5EE"),
        JobCategory(68, "Warehouse Worker", "عامل مستودع", "Retail", Icons.Default.Warehouse, "#E1F5EE"),
        JobCategory(69, "Inventory Control", "مراقب مخزون", "Retail", Icons.Default.Inventory2, "#E1F5EE"),

        // Engineering
        JobCategory(70, "Mechanical Engineer", "مهندس ميكانيكي", "Engineering", Icons.Default.SettingsApplications, "#E6F1FB"),
        JobCategory(71, "Civil Engineer", "مهندس مدني", "Engineering", Icons.Default.Foundation, "#E6F1FB"),
        JobCategory(72, "Structural Engineer", "مهندس إنشائي", "Engineering", Icons.Default.Architecture, "#E6F1FB"),
        JobCategory(73, "AutoCAD Designer", "مصمم أوتوكاد", "Engineering", Icons.Default.Draw, "#E6F1FB"),
        JobCategory(74, "Project Manager", "مدير مشروع", "Engineering", Icons.Default.ManageAccounts, "#E6F1FB"),
        JobCategory(75, "Quality Control", "مراقبة الجودة", "Engineering", Icons.Default.Verified, "#E6F1FB"),

        // Admin & HR
        JobCategory(76, "HR Manager", "مدير موارد بشرية", "Admin & HR", Icons.Default.People, "#EEEDFE"),
        JobCategory(77, "Receptionist", "موظف استقبال", "Admin & HR", Icons.Default.FrontHand, "#EEEDFE"),
        JobCategory(78, "Data Entry", "إدخال بيانات", "Admin & HR", Icons.Default.Keyboard, "#EEEDFE"),
        JobCategory(79, "Secretary", "سكرتير / سكرتيرة", "Admin & HR", Icons.Default.EditNote, "#EEEDFE"),
        JobCategory(80, "Office Manager", "مدير مكتب", "Admin & HR", Icons.Default.BusinessCenter, "#EEEDFE"),
        JobCategory(81, "Legal Advisor", "مستشار قانوني", "Admin & HR", Icons.Default.Gavel, "#EEEDFE"),

        // Facility
        JobCategory(82, "Cleaning Staff", "عامل نظافة", "Facility", Icons.Default.CleaningServices, "#F1EFE8"),
        JobCategory(83, "Gardener", "بستاني", "Facility", Icons.Default.Yard, "#F1EFE8"),
        JobCategory(84, "Maintenance", "صيانة عامة", "Facility", Icons.Default.HomeRepairService, "#F1EFE8"),
        JobCategory(85, "Pest Control", "مكافحة الحشرات", "Facility", Icons.Default.BugReport, "#F1EFE8"),
        JobCategory(86, "Laundry", "غسيل وكي", "Facility", Icons.Default.LocalLaundryService, "#F1EFE8"),

        // Real Estate
        JobCategory(87, "Real Estate Agent", "وسيط عقاري", "Real Estate", Icons.Default.RealEstateAgent, "#EAF3DE"),
        JobCategory(88, "Property Manager", "مدير عقارات", "Real Estate", Icons.Default.VpnKey, "#EAF3DE"),
        JobCategory(89, "Interior Designer", "مصمم داخلي", "Real Estate", Icons.Default.Chair, "#EAF3DE"),
        JobCategory(90, "Surveyor", "مساح", "Real Estate", Icons.Default.Map, "#EAF3DE"),

        // Beauty & Wellness
        JobCategory(91, "Hairdresser", "حلاق / كوافير", "Beauty & Wellness", Icons.Default.ContentCut, "#FBEAF0"),
        JobCategory(92, "Beautician", "أخصائية تجميل", "Beauty & Wellness", Icons.Default.FaceRetouchingNatural, "#FBEAF0"),
        JobCategory(93, "Fitness Trainer", "مدرب لياقة", "Beauty & Wellness", Icons.Default.FitnessCenter, "#FBEAF0"),
        JobCategory(94, "Massage Therapist", "معالج بالتدليك", "Beauty & Wellness", Icons.Default.Spa, "#FBEAF0"),

        // Other
        JobCategory(95, "Translator", "مترجم", "Other", Icons.Default.Translate, "#EEEDFE"),
        JobCategory(96, "Customer Service", "خدمة العملاء", "Other", Icons.Default.SupportAgent, "#EEEDFE"),
        JobCategory(97, "Tailor", "خياط / خياطة", "Other", Icons.Default.Checkroom, "#EEEDFE"),
        JobCategory(98, "Mechanic", "ميكانيكي", "Other", Icons.Default.CarRepair, "#EEEDFE"),
        JobCategory(99, "Agriculture", "مزارع / زراعة", "Other", Icons.Default.Grass, "#EEEDFE"),
        JobCategory(100, "Freelancer", "عمل حر", "Other", Icons.Default.LaptopMac, "#EEEDFE")
    )

    val jobCategoriesWithIcons: Map<String, ImageVector> = saudiCategories.associate { it.nameEn to it.icon }

    var dynamicCategoriesList: List<Category> = emptyList()

    val currentCategories: List<JobCategory>
        get() {
            if (dynamicCategoriesList.isNotEmpty()) {
                return dynamicCategoriesList.map { dyn ->
                    val idInt = try { dyn.id.toInt() } catch(e: Exception) { dyn.id.hashCode() }
                    JobCategory(
                        id = idInt,
                        nameEn = dyn.nameEn,
                        nameAr = dyn.nameAr,
                        sector = dyn.sector,
                        icon = getIconByString(dyn.icon),
                        colorHex = "#E6F1FB"
                    )
                }
            }
            return saudiCategories
        }

    fun getIconByString(key: String): ImageVector {
        return when (key.lowercase().trim()) {
            "construction" -> Icons.Default.Construction
            "plumbing" -> Icons.Default.Plumbing
            "electrical", "electrical_work" -> Icons.Default.ElectricalServices
            "carpentry" -> Icons.Default.Handyman
            "painting" -> Icons.Default.FormatPaint
            "welding" -> Icons.Default.LocalFireDepartment
            "ac", "ac_cooling", "ac_&_cooling", "air_conditioning" -> Icons.Default.AcUnit
            "tiling", "flooring", "tiling_&_flooring" -> Icons.Default.GridView
            "code", "software", "development", "software_engineer", "web_developer", "mobile_developer" -> Icons.Default.Code
            "web", "website" -> Icons.Default.Language
            "phone", "mobile", "mobile_dev" -> Icons.Default.PhoneAndroid
            "design", "ui_ux", "ui/ux", "graphic_designer" -> Icons.Default.DesignServices
            "data", "analyst", "data_analyst" -> Icons.Default.BarChart
            "security", "cybersecurity" -> Icons.Default.Security
            "router", "network", "network_engineer" -> Icons.Default.Router
            "support", "it", "it_support" -> Icons.Default.SupportAgent
            "health", "doctor", "nurse", "healthcare" -> Icons.Default.LocalHospital
            "healing" -> Icons.Default.Healing
            "school", "education", "teacher" -> Icons.Default.School
            "car", "transport", "directions_car", "driver" -> Icons.Default.DirectionsCar
            "balance", "finance", "bank", "accountant" -> Icons.Default.AccountBalance
            "restaurant", "hospitality", "food", "chef" -> Icons.Default.Restaurant
            "clean", "cleaning", "cleaning_staff" -> Icons.Default.CleaningServices
            "oil", "gas", "energy", "oil_&_gas", "oil_barrel" -> Icons.Default.OilBarrel
            else -> Icons.Default.Work
        }
    }

    fun getCategoryById(id: String): JobCategory? {
        val dyn = dynamicCategoriesList.find { it.id == id || it.nameEn.equals(id, ignoreCase = true) }
        if (dyn != null) {
            val idInt = try { dyn.id.toInt() } catch(e: Exception) { dyn.id.hashCode() }
            return JobCategory(
                id = idInt,
                nameEn = dyn.nameEn,
                nameAr = dyn.nameAr,
                sector = dyn.sector,
                icon = getIconByString(dyn.icon),
                colorHex = "#E6F1FB"
            )
        }
        return saudiCategories.find { it.id.toString().equals(id, ignoreCase = true) || it.nameEn.equals(id, ignoreCase = true) }
    }

    fun getActiveCategoriesEn(): List<String> {
        val dyn = dynamicCategoriesList
        if (dyn.isNotEmpty()) {
            return dyn.filter { it.isActive }.map { it.nameEn }
        }
        return saudiCategories.map { it.nameEn }
    }

    fun getCategoryName(id: String, lang: String): String {
        val cat = getCategoryById(id)
        if (cat != null) {
            return if (lang == "ar") cat.nameAr else cat.nameEn
        }
        return id
    }

    fun getCategoryColor(id: String): Color {
        val cat = getCategoryById(id) ?: return Color(0xFF9E9E9E)
        return try {
            Color(android.graphics.Color.parseColor(cat.colorHex))
        } catch (e: Exception) {
            Color(0xFF9E9E9E)
        }
    }

    fun getIconForCategory(category: String): ImageVector {
        val cat = getCategoryById(category)
        return cat?.icon ?: Icons.Default.Work
    }
}
