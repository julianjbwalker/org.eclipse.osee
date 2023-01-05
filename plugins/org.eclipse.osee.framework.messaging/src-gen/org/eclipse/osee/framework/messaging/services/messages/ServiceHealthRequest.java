/*********************************************************************
 * Copyright (c) 2023 Boeing
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Boeing - initial API and implementation
 **********************************************************************/
//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2010.03.30 at 03:47:04 PM MST
//

package org.eclipse.osee.framework.messaging.services.messages;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Java class for ServiceHealthRequest complex type.
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ServiceHealthRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="serviceName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serviceVersion" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="serviceDescription" type="{}ServiceDescriptionPair" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceHealthRequest", propOrder = {"serviceName", "serviceVersion", "serviceDescription"})
public class ServiceHealthRequest {

   @XmlElement(required = true)
   protected String serviceName;
   @XmlElement(required = true)
   protected String serviceVersion;
   @XmlElement(required = true)
   protected List<ServiceDescriptionPair> serviceDescription;

   /**
    * Gets the value of the serviceName property.
    * 
    * @return possible object is {@link String }
    */
   public String getServiceName() {
      return serviceName;
   }

   /**
    * Sets the value of the serviceName property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setServiceName(String value) {
      this.serviceName = value;
   }

   /**
    * Gets the value of the serviceVersion property.
    * 
    * @return possible object is {@link String }
    */
   public String getServiceVersion() {
      return serviceVersion;
   }

   /**
    * Sets the value of the serviceVersion property.
    * 
    * @param value allowed object is {@link String }
    */
   public void setServiceVersion(String value) {
      this.serviceVersion = value;
   }

   /**
    * Gets the value of the serviceDescription property.
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the serviceDescription property.
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getServiceDescription().add(newItem);
    * </pre>
    * <p>
    * Objects of the following type(s) are allowed in the list {@link ServiceDescriptionPair }
    */
   public List<ServiceDescriptionPair> getServiceDescription() {
      if (serviceDescription == null) {
         serviceDescription = new ArrayList<>();
      }
      return this.serviceDescription;
   }

}
