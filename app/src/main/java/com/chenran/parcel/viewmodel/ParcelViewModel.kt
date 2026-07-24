package com.chenran.parcel.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chenran.parcel.model.ParcelData
import com.chenran.parcel.model.SmsData
import com.chenran.parcel.model.SmsModel
import com.chenran.parcel.util.SmsProcessor
import com.chenran.parcel.util.SmsParser
import com.chenran.parcel.util.ai.AiConfig
import com.chenran.parcel.util.ai.ParseLedger
import com.chenran.parcel.util.ai.ParseStatus
import com.chenran.parcel.util.ai.ParseStrategy
import com.chenran.parcel.util.getAddressMappings
import com.chenran.parcel.util.getAiConfig
import com.chenran.parcel.util.getCustomList
import com.chenran.parcel.util.getSortByLocker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FailedMessageUiModel(
    val message: SmsModel,
    val aiStatus: ParseStatus? = null,
    val aiError: String = "",
    val showAiStatus: Boolean = false,
    val canRetry: Boolean = false,
)

class ParcelViewModel(
    private val smsParser: SmsParser = SmsParser(),
    private val context: Context? = null
) : ViewModel() {
    // 所有短信列表
    private val _allMessages = MutableStateFlow<List<SmsModel>>(emptyList())

    // 所有已取件id列表
    private val _allCompletedIds = MutableStateFlow<List<String>>(emptyList())

    init {
        context?.let {
            val completedIds = getCustomList(it, "completedIds").toMutableList()
            _allCompletedIds.value = completedIds
        }
    }

    // 解析成功的短信
    private val _successSmsData = MutableStateFlow<List<SmsData>>(emptyList())
    val successSmsData: StateFlow<List<SmsData>> = _successSmsData

    // 解析失败的短信（已预计算 AI 状态，避免列表重组时同步查库）
    private val _failedMessages = MutableStateFlow<List<FailedMessageUiModel>>(emptyList())
    val failedMessages: StateFlow<List<FailedMessageUiModel>> = _failedMessages

    // 同一地址的取件码列表
    private val _parcelsData = MutableStateFlow<List<ParcelData>>(emptyList())
    val parcelsData: StateFlow<List<ParcelData>> = _parcelsData.asStateFlow()

    // 时间过滤器
    private val _timeFilterIndex = MutableStateFlow(0)
    val timeFilterIndex: StateFlow<Int> = _timeFilterIndex.asStateFlow()

    fun setTimeFilterIndex(i: Int) {
        _timeFilterIndex.value = i
    }

    fun setAllCompletedIds(list: List<String>) {
        _allCompletedIds.value = list
    }

    fun addCompletedIds(list: List<String>) {
        val data = _allCompletedIds.value.toMutableList()
        data.addAll(list)
        _allCompletedIds.value = data
        _parcelsData.value = SmsProcessor.recalculateParcels(_parcelsData.value, _allCompletedIds.value)
    }

    fun removeCompletedId(key: String) {
        val data = _allCompletedIds.value.toMutableList()
        data.remove(key)
        _allCompletedIds.value = data
        _parcelsData.value = SmsProcessor.recalculateParcels(_parcelsData.value, _allCompletedIds.value)
    }

    fun clearData() {
        _successSmsData.value = emptyList()
        _failedMessages.value = emptyList()
        _parcelsData.value = emptyList()
    }

    fun getAllMessage(list: List<SmsModel>) {
        _allMessages.value = list
        handleReceivedSms()
    }
    
    fun getAllMessageWithCustom(list: List<SmsModel>, customSmsList: List<SmsModel>) {
        val combinedList = list + customSmsList
        _allMessages.value = combinedList
        handleReceivedSms()
    }

    private var processJob: kotlinx.coroutines.Job? = null

    // 处理接收到的短信
    fun handleReceivedSms() {
        clearData()
        // 取消上一次未完成的解析，避免旧协程结果覆盖新结果
        processJob?.cancel()
        processJob = viewModelScope.launch {
            val allMessages = _allMessages.value
            val completedIds = _allCompletedIds.value
            val addressMappings = context?.let { getAddressMappings(it) } ?: emptyMap()
            val aiConfig = context?.let { getAiConfig(it) }
            val sortByLocker = context?.let { getSortByLocker(it) } ?: false

            val result = withContext(Dispatchers.Default) {
                SmsProcessor.process(
                    messages = allMessages,
                    parser = smsParser,
                    completedIds = completedIds,
                    addressMappings = addressMappings,
                    sortByLocker = sortByLocker,
                    context = context,
                    aiConfig = aiConfig,
                )
            }

            val failedUiModels = withContext(Dispatchers.Default) {
                buildFailedMessageUiModels(result.failed, context, aiConfig)
            }

            _successSmsData.value = result.successful
            _failedMessages.value = failedUiModels
            _parcelsData.value = result.parcels
        }
    }

    private fun buildFailedMessageUiModels(
        failed: List<SmsModel>,
        context: Context?,
        aiConfig: AiConfig?,
    ): List<FailedMessageUiModel> {
        if (context == null || aiConfig == null || !aiConfig.enabled || aiConfig.parseStrategy == ParseStrategy.REGEX_ONLY) {
            return failed.map { FailedMessageUiModel(message = it) }
        }
        val canUseAi = aiConfig.apiKey.isNotBlank() && aiConfig.apiUrl.startsWith("https://", ignoreCase = true)
        val records = ParseLedger.getAll(context)
        return failed.map { message ->
            val record = records[ParseLedger.fingerprint(message)]
            FailedMessageUiModel(
                message = message,
                aiStatus = record?.status,
                aiError = record?.lastError.orEmpty(),
                showAiStatus = record != null,
                canRetry = canUseAi && record?.status != ParseStatus.PROCESSING,
            )
        }
    }

    // 将自定义规则添加到 SmsParser
    fun addCustomAddressPattern(pattern: String) {
        smsParser.addCustomAddressPattern(pattern)
    }

    fun addCustomCodePattern(pattern: String) {
        smsParser.addCustomCodePattern(pattern)
    }

    fun addCustomLockerPattern(pattern: String) {
        smsParser.addCustomLockerPattern(pattern)
    }

    fun addIgnoreKeyword(keyword: String) {
        smsParser.addIgnoreKeyword(keyword)
    }

    fun clearAllCustomPatterns() {
        smsParser.clearAllCustomPatterns()
    }



}


