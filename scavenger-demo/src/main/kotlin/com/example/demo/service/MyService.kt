package com.example.demo.service

import com.example.demo.annotation.ScavengerEnabled
import org.springframework.stereotype.Service

@ScavengerEnabled
@Service
class MyService : MyParentService(), MyInterface
