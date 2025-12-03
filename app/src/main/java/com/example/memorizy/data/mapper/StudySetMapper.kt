package com.example.memorizy.data.mapper

import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.source.network.dto.StudySetDto

// From local to network
fun StudySet.toDto(): StudySetDto {
    return StudySetDto(
        id = this.remoteId,
        name = this.name,
        description = this.description,
        iconId = this.iconId,
        createdAt = this.createdAt
    )
}

// From network to local
fun StudySetDto.toEntity(): StudySet {
    return StudySet(
        id = 0,
        name = this.name,
        description = this.description,
        iconId = this.iconId,
        createdAt = this.createdAt ?: System.currentTimeMillis(),
        remoteId = this.id,
    )
}