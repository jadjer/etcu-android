package by.jadjer.etcu.data.model

import com.google.gson.annotations.SerializedName

data class GitHubReleaseDTO(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("name") val name: String,
    @SerializedName("assets") val assets: List<GitHubAssetDTO>
)
