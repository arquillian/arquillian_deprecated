package org.jboss.arquillian.performance.ejb;

import javax.ejb.Local;

public @Local
interface WorkHard
{
   double workHard();
   
   double workVeryHard();
}
