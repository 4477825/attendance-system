package com.attendance.security;

import com.attendance.common.ErrorCode;
import com.attendance.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserDetailsServiceImpl userDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String token = extractToken(request);
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserById(userId);
                Long finalUserId = userId;
                org.springframework.security.authentication.AbstractAuthenticationToken authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        String.valueOf(finalUserId), null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (BusinessException e) {
            log.warn("JWT validation failed (business): {}", e.getMessage());
            throw e;
        } catch (UsernameNotFoundException e) {
            log.warn("JWT validation failed (user not found): {}", e.getMessage());
            throw new BusinessException(ErrorCode.USER_NOT_FOUND, e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT validation failed (invalid token): {}", e.getMessage());
            throw new BusinessException(ErrorCode.TOKEN_INVALID, "Token invalid");
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            log.debug("Unexpected JWT validation error: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}