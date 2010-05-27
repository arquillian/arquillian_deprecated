package org.jboss.arquillian.performance.ejb;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Stateless;

public @Stateless class WorkHardBean implements WorkHard
{
   @Resource EJBContext ctx;
   
   public double workHard()
   {
      System.out.println("doing some hard work here...");
      return 21;
   }

   public double workVeryHard()
   {
      System.out.println("doing some harder work here...");
      return 42;
   }
   
   public boolean isTransactional() {
      ctx.setRollbackOnly();
      return ctx.getRollbackOnly();
   }
}
