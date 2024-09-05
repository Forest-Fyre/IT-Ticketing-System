package org.seng2050.A3;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

   @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource, PasswordEncoder passwordEncoder) {
        return new JdbcUserDetailsManager(dataSource);
   }

	//main bean used to check security access across site pages
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {    

        httpSecurity.csrf(csrf -> csrf.disable());

        // add configuration
    
        httpSecurity.formLogin(formLogin -> {
            formLogin.loginPage("/login")
                .loginProcessingUrl("/perform_login")
                .defaultSuccessUrl("/homepage")
                .failureUrl("/error_login");
        });

        httpSecurity.logout(logout -> {
            logout.logoutUrl("/perform_logout")
                .invalidateHttpSession(true)
                .logoutSuccessUrl("/login.html");
        });

        httpSecurity.authorizeHttpRequests(requestConstraints -> {
                
            // every user can access the login page
            requestConstraints.requestMatchers("/login.html").permitAll();
            requestConstraints.requestMatchers("/perform_login").permitAll(); // every user can try login
            requestConstraints.requestMatchers("/login*").permitAll();
            requestConstraints.requestMatchers("/style.css").permitAll();
            requestConstraints.requestMatchers("/error_login").permitAll();

            // autheticated users can access homepage. 
            requestConstraints.requestMatchers("/error*").authenticated(); 
            requestConstraints.requestMatchers("/error**").authenticated(); 
            requestConstraints.requestMatchers("/homepage.html").authenticated();
            requestConstraints.requestMatchers("/homepage").authenticated();
            requestConstraints.requestMatchers("/issues*").authenticated();
            requestConstraints.requestMatchers("/issue**").authenticated();
            requestConstraints.requestMatchers("/KB*").authenticated();
            requestConstraints.requestMatchers("/notifications*").authenticated();
            requestConstraints.requestMatchers("/notifications.html*").authenticated();
            requestConstraints.requestMatchers("/SubmitReport*").authenticated();
            requestConstraints.requestMatchers("/mark_seen*").authenticated();
            requestConstraints.requestMatchers("/Notification_mark_seen*").authenticated();

            // only managers can access statistics
            requestConstraints.requestMatchers("/statistics*").hasRole("MANAGER");
            requestConstraints.requestMatchers("/ua_issues*").hasAnyRole("MANAGER", "IT");
			requestConstraints.requestMatchers("/completeStatus*").hasRole("IT");
            requestConstraints.requestMatchers("/a_issues*").hasRole("IT");
			
			

			//only users can report issues
			requestConstraints.requestMatchers("/report").hasRole("USER");

			requestConstraints.requestMatchers("/report").authenticated();
            requestConstraints.requestMatchers("/issues*").authenticated();
			requestConstraints.requestMatchers("/KB*").authenticated();
            requestConstraints.requestMatchers("/notifications*").authenticated();
            requestConstraints.requestMatchers("/notifications.html*").authenticated();
			requestConstraints.requestMatchers("/statistics*").hasRole("MANAGER");

            // allow anybody to access the home page
            requestConstraints.requestMatchers("/login.html").authenticated();
            requestConstraints.requestMatchers("/homepage.html").authenticated();
            
            // allow anybody to access the login form
            requestConstraints.requestMatchers("/login*").permitAll();
            requestConstraints.requestMatchers("/notifications*").permitAll();
            requestConstraints.requestMatchers("/notifications*").permitAll();
            // allow anybody to access any other page (e.g. out quiz, cart or movie applications)
            //requestConstraints.requestMatchers("/**").permitAll();
            requestConstraints.requestMatchers("/selfAssign").hasRole("IT");
			requestConstraints.requestMatchers("/assignStaff").hasRole("MANAGER");
			requestConstraints.requestMatchers("/completeStatus").hasRole("IT");
            requestConstraints.requestMatchers("/highlightComment").hasRole("IT");
			requestConstraints.requestMatchers("/acceptSolution*").hasRole("USER");
			requestConstraints.requestMatchers("/rejectSolution*").hasRole("USER");
        });

        return httpSecurity.build();
    }
}

