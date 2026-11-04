package com.moyarossiya.tv.data

data class NowResponse(
    val titel: String? = null,
    val seit: Long? = null,
)

data class NextResponse(
    val titel: String? = null,
)

data class ScheduleResponse(
    val tag: String = "",
    val eintraege: List<ScheduleEntry> = emptyList(),
)

data class ScheduleEntry(
    val start: Long = 0,
    val ende: Long = 0,
    val titel: String = "",
)
