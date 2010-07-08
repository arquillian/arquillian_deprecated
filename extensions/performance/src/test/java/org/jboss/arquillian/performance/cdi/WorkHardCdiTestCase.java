package org.jboss.arquillian.performance.cdi;

import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.performance.annotation.Performance;
import org.jboss.arquillian.performance.annotation.PerformanceTest;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@PerformanceTest(reportDegradingResults=true)
@RunWith(Arquillian.class)
public class WorkHardCdiTestCase
{

   @Deployment
   public static JavaArchive createDeployment() {
       return ShrinkWrap.create(JavaArchive.class ,"test.jar")
               .addPackage(
                       WorkHard.class.getPackage()
               )
               .addManifestResource(
                       new ByteArrayAsset("<beans/>".getBytes()),
                       ArchivePaths.create("beans.xml"));
   }
   
   @Inject @WorkHard double hardWorker;
   
   @Test
   @Performance(time=20)
   public void doHardWork() throws Exception 
   {
      Assert.assertEquals(21, hardWorker, 0d);
   }
   
   /**
    * This method is supposed to fail!
    * 
    * @throws Exception
    */
   @Test
   //@Performance(time=0.01)
   @Performance(time=10)
   public void doHardWorkThatFails() throws Exception
   {
      Assert.assertEquals(21, hardWorker, 0d);
   }
}
