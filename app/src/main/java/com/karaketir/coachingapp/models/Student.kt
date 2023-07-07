package com.karaketir.coachingapp.models

import java.io.Serializable

class Student(
    var studentName: String, var teacher: String, var id: String, var grade: Int
) : Serializable