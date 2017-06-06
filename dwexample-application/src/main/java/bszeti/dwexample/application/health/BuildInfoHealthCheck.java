package bszeti.dwexample.application.health;

import java.io.File;
import java.util.jar.Manifest;

import static org.apache.commons.io.FileUtils.*;

import com.codahale.metrics.health.HealthCheck;

/**
 * Return some information about the given path in health check
 */
public class BuildInfoHealthCheck extends HealthCheck {
	@Override
	protected Result check() throws Exception {
		
		Manifest manifest=new Manifest(getClass().getClassLoader().getResource("META-INF/MANIFEST.MF").openStream());
		
		return HealthCheck.Result.builder()
				.withDetail("Implementation-Version", manifest.getMainAttributes().getValue("Implementation-Version"))
				.withDetail("Implementation-Build-Number", manifest.getMainAttributes().getValue("Implementation-Build-Number"))
				.withDetail("Implementation-Build-Commit", manifest.getMainAttributes().getValue("Implementation-Build-Commit"))
				.withDetail("Implementation-Build-Branch", manifest.getMainAttributes().getValue("Implementation-Build-Branch"))
				.build();
	}

}
