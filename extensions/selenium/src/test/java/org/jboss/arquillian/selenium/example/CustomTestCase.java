package org.jboss.arquillian.selenium.example;

import static org.jboss.arquillian.api.RunModeType.AS_CLIENT;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.api.Run;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.selenium.annotation.Selenium;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.thoughtworks.selenium.DefaultSelenium;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 * 
 * Uses legacy Selenium driver bound to Firefox browser.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class CustomTestCase
{
   /**
    * This creates the Selenium driver before invocation of test methods
    */
   @Selenium(instantiator = CustomInstantiator.class)
   private DefaultSelenium driver;

   private static final String USERNAME = "demo";
   private static final String PASSWORD = "demo";

   private static final String LOGGED_IN = "xpath=//li[contains(text(),'Welcome')]";
   private static final String LOGGED_OUT = "xpath=//li[contains(text(),'Goodbye')]";

   private static final String USERNAME_FIELD = "id=loginForm:username";
   private static final String PASSWORD_FIELD = "id=loginForm:password";

   private static final String LOGIN_BUTTON = "id=loginForm:login";
   private static final String LOGOUT_BUTTON = "id=loginForm:logout";;

   private static final String TIMEOUT = "15000";

   /**
    * Loads already existing WAR of Weld Login JSF example
    * 
    * @return WebArchive to be tested
    */
   @Deployment
   public static WebArchive createDeployment()
   {
      try
      {
         ZipFile war = new ZipFile(new File("src/test/resources/weld-login.war"));
         return ShrinkWrap.create(ZipImporter.class, "weld-login.war").importZip(war).as(WebArchive.class);
      }
      catch (IOException e)
      {
         e.printStackTrace();
         throw new RuntimeException("Unable to load testing WAR application");
      }
   }

   @Test
   public void testLoginAndLogout()
   {
      driver.open("http://localhost:8080/weld-login/home.jsf");

      driver.type(USERNAME_FIELD, USERNAME);
      driver.type(PASSWORD_FIELD, PASSWORD);
      driver.click(LOGIN_BUTTON);
      driver.waitForPageToLoad(TIMEOUT);

      Assert.assertTrue("User should be logged in!", driver.isElementPresent(LOGGED_IN));

      driver.click(LOGOUT_BUTTON);
      driver.waitForPageToLoad(TIMEOUT);
      Assert.assertTrue("User should not be logged in!", driver.isElementPresent(LOGGED_OUT));
   }

}