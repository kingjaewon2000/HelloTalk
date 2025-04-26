package com.example.apiserver.global.config

import com.linecorp.kotlinjdsl.render.jpql.JpqlRenderContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class KotlinJdslConfig {

    @Bean
    fun jdslRenderContext() : JpqlRenderContext {
        return JpqlRenderContext()
    }

}