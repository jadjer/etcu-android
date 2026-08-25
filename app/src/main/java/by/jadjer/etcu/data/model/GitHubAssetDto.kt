package by.jadjer.etcu.data.model

import com.google.gson.annotations.SerializedName

data class GitHubAssetDto(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("size") val size: Long
)
