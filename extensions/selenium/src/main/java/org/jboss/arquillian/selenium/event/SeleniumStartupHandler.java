package org.jboss.arquillian.selenium.event;

import org.jboss.arquillian.spi.Context;
import org.jboss.arquillian.spi.TestClass;
import org.jboss.arquillian.spi.event.suite.ClassEvent;
import org.jboss.arquillian.spi.event.suite.EventHandler;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

/**
 * Creates a Selenium WebDriver and binds it to the current context.
 * 
 * This driver is registered under WebDriver.class
 * 
 * @author <a href="mailto:kpiwko@redhat.com">Karel Piwko</a>
 * 
 */
public class SeleniumStartupHandler implements EventHandler<ClassEvent> {

   /**
    * Context holder for Selenium WebDriver instance
    */
	public static final Class<WebDriver> SELENIUM = WebDriver.class;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jboss.arquillian.spi.event.suite.EventHandler#callback(org.jboss.
	 * arquillian.spi.Context, java.lang.Object)
	 */
	public void callback(Context context, ClassEvent event) throws Exception {

		createSelenium(context, event.getTestClass());

	}

	private void createSelenium(Context context, TestClass testClass) {

		WebDriver webDriver = new HtmlUnitDriver();

		context.add(SELENIUM, webDriver);

	}

}
