package com.example.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.outlined.Group
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.outlined.SupervisorAccount

sealed class Screen(val route: String, val titleResId: Int, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Search : Screen("search", R.string.search, Icons.Filled.Search, Icons.Outlined.Search)
    object Login : Screen("login", R.string.login, Icons.Filled.Person, Icons.Outlined.Person)
    object Jobs : Screen("jobs", R.string.jobs, Icons.Filled.Work, Icons.Outlined.Work)
    object Workers : Screen("workers", R.string.workers, Icons.Filled.Person, Icons.Outlined.Person)
    object Post : Screen("post", R.string.post, Icons.Filled.Add, Icons.Filled.Add)
    object Profile : Screen("profile", R.string.profile, Icons.Filled.Person, Icons.Outlined.Person)
    object Notifications : Screen("notifications", R.string.notifications, Icons.Filled.Notifications, Icons.Outlined.Notifications)
    object Support : Screen("support", R.string.support, Icons.Filled.Email, Icons.Outlined.Email)
    object SupportAdmin : Screen("support_admin", R.string.admin_panel, Icons.Filled.SupervisorAccount, Icons.Outlined.SupervisorAccount)
    object CompanyProfile : Screen("company_profile/{companyId}", R.string.profile, Icons.Filled.Person, Icons.Outlined.Person) {
        fun createRoute(companyId: String) = "company_profile/$companyId"
    }
    object JobDetails : Screen("job_details/{jobId}", R.string.jobs, Icons.Filled.Work, Icons.Outlined.Work) {
        fun createRoute(jobId: String) = "job_details/$jobId"
    }
    object MyApplications : Screen("my_applications", R.string.my_applications, Icons.Filled.Work, Icons.Outlined.Work)
    object JobApplicants : Screen("job_applicants/{jobId}/{jobTitle}", R.string.applied_jobs, Icons.Filled.Group, Icons.Outlined.Group) {
        fun createRoute(jobId: String, jobTitle: String) = "job_applicants/$jobId/${android.net.Uri.encode(jobTitle)}"
    }
    object Premium : Screen("premium", R.string.premium, Icons.Filled.Person, Icons.Outlined.Person)
    object SavedJobs : Screen("saved_jobs", R.string.saved_jobs, Icons.Filled.Work, Icons.Outlined.Work)
    object AppliedJobs : Screen("applied_jobs", R.string.applied_jobs, Icons.Filled.Work, Icons.Outlined.Work)
    object ChatList : Screen("chat_list", R.string.messages, Icons.Filled.Email, Icons.Outlined.Email)
    object ChatDetail : Screen("chat_detail/{roomId}?otherUserId={otherUserId}&otherUserName={otherUserName}", R.string.messages, Icons.Filled.Email, Icons.Outlined.Email) {
        fun createRoute(roomId: String, otherUserId: String = "", otherUserName: String = "") = 
            "chat_detail/$roomId?otherUserId=${android.net.Uri.encode(otherUserId)}&otherUserName=${android.net.Uri.encode(otherUserName)}"
    }
    
    object AdminDashboard : Screen("admin_dashboard", R.string.admin_panel, Icons.Filled.SupervisorAccount, Icons.Outlined.SupervisorAccount)
    object AdminUsers : Screen("admin_users", R.string.admin_panel, Icons.Filled.Person, Icons.Outlined.Person)
    object AdminJobs : Screen("admin_jobs", R.string.admin_panel, Icons.Filled.Work, Icons.Outlined.Work)
    object AdminWorkers : Screen("admin_workers", R.string.admin_panel, Icons.Filled.Person, Icons.Outlined.Person)
    object AdminBoostPlans : Screen("admin_boost_plans", R.string.admin_panel, Icons.Filled.Work, Icons.Outlined.Work)
    object AdminCategories : Screen("admin_categories", R.string.admin_panel, Icons.Filled.Work, Icons.Outlined.Work)
    object AdminNotifications : Screen("admin_notifications", R.string.admin_panel, Icons.Filled.Notifications, Icons.Outlined.Notifications)
    object AdminRevenue : Screen("admin_revenue", R.string.admin_panel, Icons.Filled.Work, Icons.Outlined.Work)
    object AdminSettings : Screen("admin_settings", R.string.admin_panel, Icons.Filled.Work, Icons.Outlined.Work)
    object AdminLogs : Screen("admin_logs", R.string.admin_panel, Icons.Filled.Warning, Icons.Outlined.Warning)
    object AdminReports : Screen("admin_reports", R.string.admin_panel, Icons.Filled.Warning, Icons.Outlined.Warning)
    object AdminCampaigns : Screen("admin_campaigns", R.string.admin_panel, Icons.Filled.Work, Icons.Outlined.Work)
}
