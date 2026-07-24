package com.chenran.parcel.util.ai

data class AiProviderPreset(
    val id: String,
    val name: String,
    val tag: String,
    val category: String,
    val apiUrl: String,
    val defaultModel: String,
    val popularModels: List<String>,
    val desc: String,
)

val AI_PROVIDER_PRESETS = listOf(
    AiProviderPreset("siliconflow", "硅基流动 SiliconFlow", "国内主流大模型集合", "国内", "https://api.siliconflow.cn/v1/chat/completions", "deepseek-ai/DeepSeek-V3", listOf("deepseek-ai/DeepSeek-V3", "deepseek-ai/DeepSeek-R1", "Qwen/Qwen2.5-72B-Instruct", "THUDM/glm-4-9b-chat"), "聚合 DeepSeek、Qwen、GLM 等国内模型"),
    AiProviderPreset("deepseek", "DeepSeek 深度求索", "极致性价比", "国内", "https://api.deepseek.com/v1/chat/completions", "deepseek-chat", listOf("deepseek-chat", "deepseek-reasoner"), "适合复杂中文短信解析"),
    AiProviderPreset("qwen", "阿里通义千问", "阿里云兼容接口", "国内", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen-turbo", listOf("qwen-turbo", "qwen-plus", "qwen-max", "qwen-long"), "中文通知提取能力稳定"),
    AiProviderPreset("bailian", "阿里百炼", "阿里云平台", "国内", "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions", "qwen-plus", listOf("qwen-plus", "qwen-turbo", "qwen-max"), "OpenAI 规范兼容端点"),
    AiProviderPreset("zhipu", "智谱 AI (GLM)", "高速响应", "国内", "https://open.bigmodel.cn/api/paas/v4/chat/completions", "glm-4-flash", listOf("glm-4-flash", "glm-4-air", "glm-4-plus"), "适合频繁识别"),
    AiProviderPreset("doubao", "字节跳动豆包", "字节大模型", "国内", "https://ark.cn-beijing.volces.com/api/v3/chat/completions", "doubao-pro-4k", listOf("doubao-pro-4k", "doubao-lite-4k", "doubao-pro-32k"), "中文指令遵循能力强"),
    AiProviderPreset("volcengine", "火山引擎方舟", "字节云平台", "国内", "https://ark.cn-beijing.volces.com/api/v3/chat/completions", "ep-xxxxxxxx", listOf("doubao-pro-32k", "doubao-lite-128k"), "方舟 OpenAI 兼容通道"),
    AiProviderPreset("moonshot", "Moonshot Kimi", "长文本专家", "国内", "https://api.moonshot.cn/v1/chat/completions", "moonshot-v1-8k", listOf("moonshot-v1-8k", "moonshot-v1-32k", "moonshot-v1-128k"), "适合长短信与多网点"),
    AiProviderPreset("hunyuan", "腾讯混元", "腾讯云大模型", "国内", "https://api.hunyuan.tencent.com/v1/chat/completions", "hunyuan-lite", listOf("hunyuan-lite", "hunyuan-standard", "hunyuan-pro"), "腾讯云原生中文引擎"),
    AiProviderPreset("minimax", "MiniMax", "高智能中文对话", "国内", "https://api.minimax.chat/v1/text/chatcompletion_v2", "abab6.5s-chat", listOf("abab6.5s-chat", "abab6.5t-chat", "abab7-chat"), "高性能中文模型"),
    AiProviderPreset("baichuan", "百川智能", "高准确率", "国内", "https://api.baichuan-ai.com/v1/chat/completions", "Baichuan4", listOf("Baichuan4", "Baichuan3-Turbo", "Baichuan2-Turbo"), "商业模型兼容端点"),
    AiProviderPreset("stepfun", "阶跃星辰", "多模态与长文本", "国内", "https://api.stepfun.com/v1/chat/completions", "step-1-8k", listOf("step-1-8k", "step-1-32k", "step-2-16k"), "阶跃系列服务接口"),
    AiProviderPreset("nvidia", "英伟达 NVIDIA NIM", "高性能加速", "国际", "https://integrate.api.nvidia.com/v1/chat/completions", "meta/llama-3.1-70b-instruct", listOf("meta/llama-3.1-70b-instruct", "mistralai/mixtral-8x22b-instruct"), "NVIDIA 云端模型"),
    AiProviderPreset("custom", "自定义 OpenAI 规范接口", "自由扩展", "自定义", "https://api.openai.com/v1/chat/completions", "gpt-4o-mini", listOf("gpt-4o-mini", "qwen2.5:7b", "llama3.2", "deepseek-r1:8b"), "仅支持 HTTPS 的自定义代理"),
)

fun providerById(id: String): AiProviderPreset =
    AI_PROVIDER_PRESETS.firstOrNull { it.id == id } ?: AI_PROVIDER_PRESETS.first { it.id == "deepseek" }
