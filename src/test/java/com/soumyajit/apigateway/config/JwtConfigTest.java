package com.soumyajit.apigateway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class JwtConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Configuration
    @EnableConfigurationProperties(JwtConfig.class)
    static class TestConfig { }

    @Test
    @DisplayName("Should bind properties from prefix 'jwt'")
    void testJwtConfigBinding() {
        contextRunner
                .withPropertyValues(
                        "jwt.secret=mySuperSecretKey123",
                        "jwt.expiration=3600000"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(JwtConfig.class);
                    JwtConfig config = context.getBean(JwtConfig.class);

                    assertThat(config.getSecret()).isEqualTo("mySuperSecretKey123");
                    assertThat(config.getExpiration()).isEqualTo(3600000L);
                });
    }

    @Test
    @DisplayName("Should handle default values or nulls when properties are missing")
    void testJwtConfigMissingProperties() {
        contextRunner
                .run(context -> {
                    JwtConfig config = context.getBean(JwtConfig.class);
                    assertThat(config.getSecret()).isNull();
                    assertThat(config.getExpiration()).isZero();
                });
    }
}