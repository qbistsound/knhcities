package knh.cities;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.ResourceUtils;

@SpringBootApplication
public class application 
{
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
	public static void main(String[] args) 
	{
		try { system.init(ResourceUtils.getFile("classpath:config.json").toString()); } 
		catch (Exception exception) { exception.printStackTrace(); }
		SpringApplication.run(application.class, args);
	}
	/*------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------*/
}
