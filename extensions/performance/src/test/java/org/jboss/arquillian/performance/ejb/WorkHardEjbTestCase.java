package org.jboss.arquillian.performance.ejb;

import javax.ejb.EJB;

import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(org.jboss.arquillian.junit.Arquillian.class)
public class WorkHardEjbTestCase
{
   
   @EJB
   private WorkHard hardWorker;

   @Deployment
   public static JavaArchive createTestArchive() {
      return ShrinkWrap.create("test.jar", JavaArchive.class)
         .addClasses(WorkHard.class, WorkHardBean.class);
   }
   
   @Test
   public void testHardWorker()
   {
      Assert.assertEquals(21d, hardWorker.workHard(), 0d);
   }
}
