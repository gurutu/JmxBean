package org.jmxbean;

import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;
import org.jmxbean.annotations.MBeanAttribute;
import org.jmxbean.annotations.MBeanOperation;
import org.jmxbean.annotations.MBeanParameter;


import javax.management.*;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DynamicMbeanWrapper implements DynamicMBean {

    private static final Logger log=LoggerFactory.getLogger(DynamicMbeanWrapper.class);
    private static final Map<String,Class<?>> primitiveTypeMap= Collections.unmodifiableMap(new HashMap<String,Class<?>>(){
        {
         this.put("boolean",Boolean.class);
         this.put("byte", Byte.class);
         this.put("char", Character.class);
         this.put("short", Short.class);
         this.put("int", Integer.class);
         this.put("long", Long.class);
         this.put("float", Float.class);
         this.put("double", Double.class);
         this.put("void", Void.class);
        }
    });
    private final Object target;
    private final Map<String, MBeanAttributeInfo> attribute;


    public DynamicMbeanWrapper(Object target, String description) {
        this.target =target;
        this.attribute=this.getAttributes(target);
        MBeanOperationInfo[] operations=this.getOperations(target);
    }

    private MBeanOperationInfo[] getOperations(Object target) {
        List<MBeanOperationInfo> operations=new ArrayList<>();
        this.getOperationsRecursive(target.getClass(),operations);
        return operations.toArray(new MBeanOperationInfo[0]);
    }

    private void getOperationsRecursive(Class<?> aClass, List<MBeanOperationInfo> operations) {
        for (Method method:aClass.getDeclaredMethods()){
            if(method.isAnnotationPresent(MBeanOperation.class)){
                MBeanOperation operationAnnotation = method.getAnnotation(MBeanOperation.class);
                MBeanParameterInfo[] parameters=this.getParameters(method.getParameters());
                String returnType = method.getReturnType().getName();
                String description = operationAnnotation.description();
                MBeanOperationInfo mBeanOperationInfo = new MBeanOperationInfo(method.getName(), description != null && !description.isEmpty() ? description : method.getName(), parameters, returnType, operationAnnotation.impact());
                operations.add(mBeanOperationInfo);
            }
        }

        //checking for interface
        for (Class<?>cls:aClass.getInterfaces()){
            this.getOperationsRecursive(cls,operations);
        }
        //checking for superclass
        Class<?> superclass = aClass.getSuperclass();
        if(superclass!=null){
            this.getOperationsRecursive(superclass,operations);
        }
    }

    private MBeanParameterInfo[] getParameters(Parameter[] parameters) {
        MBeanParameterInfo[] parameterInfos=new MBeanParameterInfo[parameters.length];
        int i=0;
        for(Parameter parameter:parameters){
            String description="";
            if(parameter.isAnnotationPresent(MBeanParameter.class)){
                MBeanParameter annotation = parameter.getAnnotation(MBeanParameter.class);
                description=annotation.description();
            }
            parameterInfos[i]=new MBeanParameterInfo(parameter.getName(),parameter.getType().getName(),description!=null&&!description.isEmpty()?description: parameter.getName());
            i++;
        }
        return parameterInfos;
    }

    private Map<String, MBeanAttributeInfo> getAttributes(Object target){
        Map<String, MBeanAttributeInfo> attributes=new HashMap<>();
        this.getAttributesRecursive(target.getClass(),attributes);
        return attributes;

    }

    private void getAttributesRecursive(Class<?> aClass, Map<String, MBeanAttributeInfo> attributes) {
        Method[] var3=aClass.getDeclaredMethods();
        for (Method method:var3){
              if(method.isAnnotationPresent(MBeanAttribute.class)){
                  MBeanAttribute mBeanAttribute=method.getAnnotation(MBeanAttribute.class);
                  String attributeName=null;
                  String attributeType=null;
                  boolean isReadable=false;
                  boolean isWritable=false;
                  boolean isIs=false;
                  int parameterCount= method.getParameterCount();
                  String methodName=method.getName();
                  if(methodName.startsWith("get")&&parameterCount==0){
                      attributeName=methodName.substring(3);
                      attributeType=method.getReturnType().getName();
                      isReadable=true;
                  } else if (methodName.startsWith("is") && parameterCount==0) {
                      attributeName=methodName.substring(2);
                      attributeType=method.getReturnType().getName();
                      isIs=true;
                  } else if (methodName.startsWith("set") && parameterCount==1) {
                      attributeName=methodName.substring(3);
                      attributeType=method.getReturnType().getName();
                      isWritable=true;
                  }

                  if(attributeName==null){
                      return;
                  }

                  attributeName= Character.toLowerCase(attributeName.charAt(0))+attributeName.substring(1);
                  if(attributes.containsKey(attributeName)){
                      MBeanAttributeInfo existingAttribute= (MBeanAttributeInfo) attributes.get(attributeName);
                      isReadable= isReadable || existingAttribute.isReadable();
                      isWritable= isWritable || existingAttribute.isWritable();
                      isIs= isIs || existingAttribute.isIs();
                  }

                  String description = mBeanAttribute.description();
                  MBeanAttributeInfo attributeInfo=new MBeanAttributeInfo(attributeName,attributeType,description!=null && !description.isEmpty()?description:attributeName,isReadable,isWritable,isIs);
                  attributes.put(attributeName,attributeInfo);
              }
        }
        Class<?>[] interfacesClass = aClass.getInterfaces();
        for (Class<?> cla:interfacesClass){
            this.getAttributesRecursive(cla,attributes);
        }

        Class<?> superclass = aClass.getSuperclass();
        if (superclass!=null){
            this.getAttributesRecursive(superclass,attributes);
        }


    }

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {

    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        return null;
    }


    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        return null;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return null;
    }
}
