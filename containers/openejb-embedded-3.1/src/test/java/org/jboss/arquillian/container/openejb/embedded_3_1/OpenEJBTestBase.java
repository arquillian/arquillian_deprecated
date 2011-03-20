package org.jboss.arquillian.container.openejb.embedded_3_1;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Queue;
import javax.sql.DataSource;

import org.jboss.arquillian.container.openejb.embedded_3_1.ejb.SimpleLocalBusiness;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class OpenEJBTestBase
{
   @EJB
   protected SimpleLocalBusiness simpleBean;

   @Resource
   protected DataSource testDatabase;
   
   @Resource
   private Queue myQueue;
}
