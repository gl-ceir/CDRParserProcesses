package com.glocks.constants;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.stereotype.Component;

@Component
// @PropertySource("classpath:application.properties")

@PropertySources({
  @PropertySource(
      value = {"file:application.properties"},
      ignoreResourceNotFound = true),
  @PropertySource(
      value = {"file:configuration.properties"},
      ignoreResourceNotFound = true)
})
public class PropertiesReader {

  @Value("${appdbName}")
  public String appdbName;

  @Value("${repdbName}")
  public String repdbName;

  @Value("${auddbName}")
  public String auddbName;

  @Value("${oamdbName}")
  public String oamdbName;

  @Value("${serverName}")
  public String serverName;

  @Value("#{'${yyMMddSource}'.split(',')}")
  public List<String> yyMMddSource;

  @Value("#{'${ddMMyySource}'.split(',')}")
  public List<String> ddMMyySource;

  @Value("#{'${ddMMyyyySource}'.split(',')}")
  public List<String> ddMMyyyySource;

  @Value("${yyyyMMddSource}")
  public String yyyyMMddSource;
}
