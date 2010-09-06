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
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

/**
 * Tests Arquillian Selenium extension against Weld Login example.
 * 
 * Uses standard settings of Selenium 2.0, that is HtmlUnitDriver.
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
@RunWith(Arquillian.class)
@Run(AS_CLIENT)
public class ArquillianSeleniumTestCase
{
   /**
    * This creates the Selenium driver before invocation of test methods
    */
   @Selenium
   private WebDriver driver;

   private static final String USERNAME = "demo";
   private static final String PASSWORD = "demo";

   private static final By LOGGED_IN = By.xpath("//li[contains(text(),'Welcome')]");
   private static final By LOGGED_OUT = By.xpath("//li[contains(text(),'Goodbye')]");

   private static final By USERNAME_FIELD = By.id("loginForm:username");
   private static final By PASSWORD_FIELD = By.id("loginForm:password");

   private static final By LOGIN_BUTTON = By.id("loginForm:login");
   private static final By LOGOUT_BUTTON = By.id("loginForm:logout");

   /**
    * Load already existing WAR of Weld Login JSF example
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
      driver.get("http://localhost:8080/weld-login/home.jsf");

      driver.findElement(USERNAME_FIELD).sendKeys(USERNAME);
      driver.findElement(PASSWORD_FIELD).sendKeys(PASSWORD);
      driver.findElement(LOGIN_BUTTON).submit();
      checkElementPresence(LOGGED_IN, "User should be logged in!");

      driver.findElement(LOGOUT_BUTTON).submit();
      checkElementPresence(LOGGED_OUT, "User should not be logged in!");

   }

   // check is element is presence on page, fails otherwise
   private void checkElementPresence(By by, String errorMsg)
   {
      try
      {
         Assert.assertTrue(errorMsg, driver.findElement(by) != null);
      }
      catch (NoSuchElementException e)
      {
         Assert.fail(errorMsg);
      }

   }

}