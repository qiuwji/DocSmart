package com.qiu.backend.common.core.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiu.backend.common.core.Result.ResultCode;
import com.qiu.backend.common.core.exception.BusinessException;
import com.qiu.backend.common.infra.cache.impl.RedisCacheService;
import com.qiu.backend.common.utils.JwtUtil;
import com.qiu.backend.common.utils.UserContextHolder;
import com.qiu.backend.modules.user.service.impl.CustomUserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final List<String> EXCLUDE_PATTERNS = List.of(
            "/api/auth/**",
            "/public/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/favicon.ico"
    );
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    private RedisCacheService cacheService;

    @Autowired
    private CustomUserDetailsServiceImpl userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return EXCLUDE_PATTERNS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = authHeader.substring(7).trim();
            if (token.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }

            // 验证 Token
            if (!JwtUtil.validateToken(token)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "无效或过期的 Token");
            }

            // 检查 Redis 中的 Token 状态
            String redisKey = JwtUtil.getRedisKeyFromToken(token);
            if (!cacheService.exists(redisKey)) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "Token 已失效");
            }

            Long userId = cacheService.get(redisKey, Long.class);
            if (userId == null) {
                throw new BusinessException(ResultCode.UNAUTHORIZED, "用户信息不存在");
            }

            // 加载用户并设置认证信息
            UserDetails userDetails = userDetailsService.loadUserByUserId(userId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserContextHolder.setUserId(userId);

            filterChain.doFilter(request, response);
        } catch (BusinessException e) {
            writeErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, e.getMessage());
        } catch (Exception e) {
            logger.error("JWT 认证异常", e);
            writeErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "认证服务异常");
        } finally {
            // 清理上下文，防止线程复用造成内存泄漏
            SecurityContextHolder.clearContext();
        }
    }

    private void writeErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, Object> body = new HashMap<>();
        body.put("code", status);
        body.put("message", message);

        String json = objectMapper.writeValueAsString(body);
        response.getWriter().write(json);
    }
}
