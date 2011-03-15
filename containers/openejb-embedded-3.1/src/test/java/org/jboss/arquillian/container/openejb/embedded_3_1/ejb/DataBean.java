package org.jboss.arquillian.container.openejb.embedded_3_1.ejb;

import javax.annotation.Resource;
import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.sql.DataSource;

@Stateless(name="dataBeans/DataLocalBusiness")
@Local(DataLocalBusiness.class)
public class DataBean implements DataLocalBusiness
{

   @Resource(name="testDatabase")
   private DataSource dataSource;

   @Override
   public boolean isDataSourceAvailable()
   {
      return dataSource != null;
   }

}
