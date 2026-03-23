package co.escuelaing.edu.login;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/hello", "/call").authenticated()
                .anyRequest().permitAll()
            )
            .httpBasic(basic -> {});
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        var user = User.withDefaultPasswordEncoder()
            .username("admin")
            .password("secret")
            .roles("USER")
            .build();
        return new InMemoryUserDetailsManager(user);
    }
}
