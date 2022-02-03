package knh.cities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class security extends WebSecurityConfigurerAdapter
{
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @Override
    protected void configure(HttpSecurity http) throws Exception 
    {
        if (system.config.bool("authentication") == true) 
        { 
        	http.csrf().disable().authorizeRequests().anyRequest().authenticated().and().httpBasic();
        }
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
    @Autowired
    public void globals(AuthenticationManagerBuilder auth) throws Exception 
    {
    	if (system.config.bool("authentication") == true)
    	{
    		PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    		auth.inMemoryAuthentication().withUser(system.config.string("user_login")).password(encoder.encode(system.config.string("user_password"))).roles("USER");
    		auth.inMemoryAuthentication().withUser(system.config.string("admin_login")).password(encoder.encode(system.config.string("admin_password"))).roles("ADMIN", "ALLOW_EDIT");
    	}
    }
    /*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
}