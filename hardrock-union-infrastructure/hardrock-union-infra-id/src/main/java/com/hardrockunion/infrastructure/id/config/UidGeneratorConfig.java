package com.hardrockunion.infrastructure.id.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.ObjectProvider;

import com.baomidou.mybatisplus.core.incrementer.IdentifierGenerator;

import io.arkx.framework.commons.uid.UidGenerator;

@Configuration
public class UidGeneratorConfig {

    @Bean
    public IdentifierGenerator identifierGenerator(ObjectProvider<UidGenerator> uidGeneratorProvider) {
        return entity -> uidGeneratorProvider.getObject().getUID();
    }
}
