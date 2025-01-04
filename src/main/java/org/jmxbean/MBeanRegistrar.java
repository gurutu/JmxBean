package org.jmxbean;

import com.sun.jmx.mbeanserver.DynamicMBean2;
import org.jmxbean.annotations.MBean;

import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Objects;
import java.util.Optional;

import static java.util.Objects.*;

public class MBeanRegistrar {
    private MBeanRegistrar(){
        throw new UnsupportedOperationException("This class is not meant to be instantiated");
    }
    public static void registerMBean(Object object) throws MalformedObjectNameException {
        registerMBean(object, ManagementFactory.getPlatformMBeanServer());
    }

    private static void registerMBean(Object object, MBeanServer platformMBeanServer) throws MalformedObjectNameException {
        requireNonNull(platformMBeanServer,"server");
        Class<?> aClass = requireNonNull(object, "object").getClass();
        MBean mBean=findMbeanAnnotation(aClass);
        if(mBean!=null){
           ObjectName objectName=objectName(aClass,mBean);
            DynamicMBean dynamicMBean=new DynamicMbeanWrapper(object,mBean.description());
        }
    }

    private static ObjectName objectName(Class<?> aClass, MBean mBean) throws MalformedObjectNameException {
        requireNonNull(aClass,"aClass");
        requireNonNull(mBean,"Mbean");
        String name= (String) Optional.ofNullable(mBean.objectName()).map(String::trim).orElse(mBean.value());
        if(name!=null){
            name=name.trim();
        }
        return new ObjectName(name!=null&&!name.isEmpty()?name: aClass.getName()+":type="+aClass.getSimpleName());
    }

    private static MBean findMbeanAnnotation(Class<?> aClass) {
        if(aClass.isAnnotationPresent(MBean.class)){
            return (MBean) aClass.getAnnotation(MBean.class);
        }else{
            Class[] val1= aClass.getInterfaces();
            int val1Length = val1.length;
            for (Class<?> iface : val1) {
                MBean mbeanAnnotation = findMbeanAnnotation(iface);
                if (mbeanAnnotation != null) {
                    return mbeanAnnotation;
                }
            }

            Class<?> superClass=aClass.getSuperclass();
            if(superClass!=null){
                return findMbeanAnnotation(superClass);
            }else{
                return null;
            }
        }
    }
}
