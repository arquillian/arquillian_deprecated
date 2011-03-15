package org.jboss.arquillian.container.openejb.embedded_3_1.ejb;

import javax.ejb.Local;
import javax.ejb.Stateless;

@Stateless
@Local(SimpleLocalBusiness.class)
public class SimpleBean implements SimpleLocalBusiness
{

   @Override
   public void ping()
   {
   }

}
