package com.example.notifilterpro.ui.categorizer

import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.notifilterpro.data.local.AppRuleDao
import com.example.notifilterpro.data.local.AppRuleEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppUiModel(
    val packageName: String,
    val appName: String,
    val category: String,
    val isWhitelisted: Boolean
)

@HiltViewModel
class AppCategorizerViewModel @Inject constructor(
    application: Application,
    private val appRuleDao: AppRuleDao
) : AndroidViewModel(application) {

    private val _allApps = MutableStateFlow<List<AppUiModel>>(emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    val appList: StateFlow<List<AppUiModel>> = combine(_allApps, _searchQuery) { apps, query ->
        if (query.isBlank()) {
            apps
        } else {
            apps.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadApps()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    private fun loadApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = getApplication<Application>().packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }

            val resolveInfos = pm.queryIntentActivities(mainIntent, PackageManager.MATCH_ALL)
            val savedRules = appRuleDao.getAllRules().first().associateBy { it.packageName }

            val mergedList = resolveInfos.mapNotNull { resolveInfo ->
                val packageName = resolveInfo.activityInfo.packageName
                if (packageName == getApplication<Application>().packageName) return@mapNotNull null

                val appName = resolveInfo.loadLabel(pm).toString()
                val rule = savedRules[packageName]

                AppUiModel(
                    packageName = packageName,
                    appName = appName,
                    // BACK TO GREEN: Uncategorized apps default to Allow
                    category = rule?.category ?: "GREEN",
                    isWhitelisted = rule?.isWhitelisted ?: true
                )
            }.sortedBy { it.appName.lowercase() }

            _allApps.value = mergedList
        }
    }

    fun updateRule(packageName: String, appName: String, category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isWhitelisted = (category == "GREEN")
            val newRule = AppRuleEntity(packageName, appName, category, isWhitelisted)

            appRuleDao.insertRule(newRule)

            _allApps.value = _allApps.value.map {
                if (it.packageName == packageName) {
                    it.copy(category = category, isWhitelisted = isWhitelisted)
                } else {
                    it
                }
            }
        }
    }
}