package com.example.opencvexampletry3

import org.jetbrains.kotlinx.multik.api.*

fun main(){
//    val a = mk.ndarray(mk[1, 2, 3])
//    println("a is $a")


    val a = mk.ndarray(mk[1, 2, 3])
    val asum = mk.math.sum(a)
    println("Sum of $a is ${asum}")
}
