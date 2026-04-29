package com.example.memorizy.data.mapper

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.network.dto.CardDto

// Из локально в сеть
fun Card.toDto(parentRemoteId: Long): CardDto {
    return CardDto(
        id = this.remoteId,
        term = this.term,
        definition = this.definition,
        definitionVariants = this.definitionVariants,
        studySetId = parentRemoteId,
        createdAt = this.createdAt,
        level = this.level,
        nextReviewDate = this.nextReviewDate,
        reviewCount = this.reviewCount,
        mistakeCount = this.mistakeCount,
        recentAnswerHistory = this.recentAnswerHistory
    )
}

// Из сети в локально
fun CardDto.toEntity(parentLocalId: Long): Card {
    return Card(
        id = 0,
        term = this.term,
        definition = this.definition,
        definitionVariants = this.definitionVariants,
        setId = parentLocalId,
        createdAt = this.createdAt ?: System.currentTimeMillis(),
        remoteId = this.id,
        level = this.level,
        nextReviewDate = this.nextReviewDate ?: System.currentTimeMillis(),
        reviewCount = this.reviewCount,
        mistakeCount = this.mistakeCount,
        recentAnswerHistory = this.recentAnswerHistory
    )
}