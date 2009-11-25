package org.jboss.arquillian.jboss;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Paths;
import org.junit.Assert;
import org.junit.Test;


public class EmbeddedDeploymentAppenderTestCase
{

   @Test
   public void shouldGenerateDependencies() throws Exception {
      
      Archive<?> archive = new EmbeddedDeploymentAppender().createArchive();
      
      Assert.assertTrue(
            "Should have added TestEnricher SPI",
            archive.contains(Paths.create("/META-INF/services/org.jboss.arquillian.spi.TestEnricher")));

      Assert.assertTrue(
            "Should have added TestEnricher Impl",
            archive.contains(Paths.create("/org/jboss/arquillian/jboss/InjectionEnricher.class")));
      
      System.out.println(archive.toString(true));
      
   }
}
