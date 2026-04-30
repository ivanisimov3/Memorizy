package com.example.memorizy.data.mapper

import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.data.source.network.dto.SessionRecordDto

// Из локально в сеть
fun SessionRecord.toDto(parentRemoteId: Long): SessionRecordDto {
    return SessionRecordDto(
        id = this.remoteId,
        studySetId = parentRemoteId,
        type = this.type,
        correctCount = this.correctCount,
        totalCount = this.totalCount,
        percentage = this.percentage,
        timestamp = this.timestamp
    )
}

// Из сети в локально
fun SessionRecordDto.toEntity(parentLocalId: Long): SessionRecord {
    return SessionRecord(
        id = 0,
        setId = parentLocalId,
        type = this.type,
        correctCount = this.correctCount,
        totalCount = this.totalCount,
        percentage = this.percentage,
        timestamp = this.timestamp ?: System.currentTimeMillis(),
        remoteId = this.id
    )
}