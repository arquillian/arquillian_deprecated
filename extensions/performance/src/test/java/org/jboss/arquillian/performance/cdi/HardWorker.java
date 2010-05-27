package org.jboss.arquillian.performance.cdi;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

@ApplicationScoped
public class HardWorker
{

   @Produces @Named @WorkHard double workingHard()
   {
      return 21;
   }
}
