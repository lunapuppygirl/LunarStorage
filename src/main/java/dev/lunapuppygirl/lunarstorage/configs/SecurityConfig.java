package dev.lunapuppygirl.lunarstorage.configs;

import dev.lunapuppygirl.lunarstorage.managers.JsonFileManager;
import dev.lunapuppygirl.lunarstorage.oauth2.OAuth2LoginSuccessHandler;
import dev.lunapuppygirl.lunarstorage.web.filters.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final JsonFileManager jsonFileManager;

    public SecurityConfig(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthFilter jwtAuthFilter, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .oauth2Login(oauth -> oauth
                        .defaultSuccessUrl("/", true)
                        .loginPage("/login")
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                )
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setSessionAttributeName("_csrf");
        return repository;
    }

    public int getMinDashboardLevel() {
        return jsonFileManager.getInt("admin.dashboard.required_level", jsonFileManager.getConfigFile(), 1000);
    }

    public int getMinAnnouncementLevel() {
        return jsonFileManager.getInt("admin.dashboard.announcements.required_level", jsonFileManager.getConfigFile(), 1000);
    }

    public int getMinManageFilesLevel() {
        return jsonFileManager.getInt("admin.dashboard.manage_files", jsonFileManager.getConfigFile(), 1000);
    }

    public int getMinManageUsersLevel() {
        return jsonFileManager.getInt("admin.dashboard.manage_users", jsonFileManager.getConfigFile(), 1000);
    }
}
