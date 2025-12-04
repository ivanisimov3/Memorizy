package com.example.memorizy.data.mapper

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.source.network.dto.CardDto
import com.example.memorizy.data.source.network.dto.StudySetDto

// From local to network
fun Card.toDto(parentRemoteId: Long): CardDto {
    return CardDto(
        id = this.remoteId,
        term = this.term,
        definition = this.definition,
        studySetId = parentRemoteId,
        createdAt = this.createdAt
    )
}

// From network to local
fun CardDto.toEntity(parentLocalId: Long): Card {
    return Card(
        id = 0,
        term = this.term,
        definition = this.definition,
        setId = parentLocalId,
        createdAt = this.createdAt ?: System.currentTimeMillis(),
        remoteId = this.id,
    )
}