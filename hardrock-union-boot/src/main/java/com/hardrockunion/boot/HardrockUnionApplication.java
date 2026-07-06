package com.hardrockunion.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.hardrockunion")
@MapperScan({
    "com.hardrockunion.platform.iam.mapper",
    "com.hardrockunion.platform.tenant.mapper",
    "com.hardrockunion.platform.message.mapper",
    "com.hardrockunion.platform.region.mapper",
    "com.hardrockunion.platform.workflow.mapper",
    "com.hardrockunion.solution.wsgm.mapper",
    "com.hardrockunion.business.project.mapper",
    "com.hardrockunion.business.merchant.mapper",
    "com.hardrockunion.business.warehouse.mapper",
    "com.hardrockunion.business.logistics.mapper"
})
public class HardrockUnionApplication {

    public static void main(String[] args) {
        SpringApplication.run(HardrockUnionApplication.class, args);
    }
}
