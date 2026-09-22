package com.checkoutline.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate-limits per authenticated user (X-User-Id, set by JwtAuthenticationFilter once it
 * validates the token) and falls back to the caller's IP for routes that don't require one
 * (e.g. /auth/**, anonymous product browsing) — so one anonymous client can't exhaust a
 * bucket shared with everyone else.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver rateLimiterKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            if (userId != null) {
                return Mono.just(userId);
            }
            var remoteAddress = exchange.getRequest().getRemoteAddress();
            String ip = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
            return Mono.just(ip);
        };
    }
}
